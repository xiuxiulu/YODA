# Initially copied from https://github.com/mesnilgr/is13 : elman.py : Jan 1, 2015

import theano
import numpy
import time
import sys
import random
import cPickle
from theano import tensor as T
from collections import OrderedDict, defaultdict, Counter


class RecurrentNeuralNetworkModel2(object):
    def __init__(self, context_feature_size, token_feature_size, hidden_layer_size, n_output_classes,
                 context_window_size):
        self.context_window_size = context_window_size

        # parameters of the model
        self.Wx = theano.shared(0.2 * numpy.random.uniform(-1.0, 1.0, (
            token_feature_size * context_window_size, hidden_layer_size)).astype(
            theano.config.floatX))
        self.Wh = theano.shared(
            0.2 * numpy.random.uniform(-1.0, 1.0, (hidden_layer_size, hidden_layer_size)).astype(theano.config.floatX))
        self.W = theano.shared(
            0.2 * numpy.random.uniform(-1.0, 1.0, (hidden_layer_size, n_output_classes)).astype(theano.config.floatX))
        self.Wc = theano.shared(
            0.2 * numpy.random.uniform(-1.0, 1.0, (context_feature_size, n_output_classes)).astype(theano.config.floatX))
        self.bh = theano.shared(numpy.zeros(hidden_layer_size, dtype=theano.config.floatX))
        self.b = theano.shared(numpy.zeros(n_output_classes, dtype=theano.config.floatX))
        self.h0 = theano.shared(numpy.zeros(hidden_layer_size, dtype=theano.config.floatX))

        # bundle
        self.params = [self.Wx, self.Wh, self.W, self.Wc, self.bh, self.b, self.h0]
        self.names = ['Wx', 'Wh', 'W', 'Wc', 'bh', 'b', 'h0']

        # x is the token features from the window
        x = T.matrix('x', dtype=theano.config.floatX)
        y = T.ivector('y')
        cx = T.vector('cx', dtype=theano.config.floatX)

        def recurrence(x_t, h_tm1, sentence_cx):
            h_t = T.nnet.sigmoid(T.dot(x_t, self.Wx) + T.dot(h_tm1, self.Wh) + self.bh)
            s_t = T.nnet.softmax(T.dot(sentence_cx, self.Wc) + T.dot(h_t, self.W) + self.b)
            return [h_t, s_t]

        [hidden_state, output_state], updates_dont_care = theano.scan(fn=recurrence,
                                                                      sequences=x,
                                                                      non_sequences=cx,
                                                                      outputs_info=[self.h0, None],
                                                                      n_steps=x.shape[0])

        p_y_given_x_sentence = output_state[:, 0, :]
        y_prediction = T.argmax(p_y_given_x_sentence, axis=1)

        # cost and gradients and learning rate
        learning_rate = T.scalar('lr')
        nll = -T.mean(T.log(p_y_given_x_sentence)
                               [T.arange(x.shape[0]), y])
        gradients = T.grad(nll, self.params)
        updates = OrderedDict((p, p - learning_rate * g) for p, g in zip(self.params, gradients))

        # theano functions
        self.classify = theano.function(inputs=[cx, x], outputs=y_prediction)
        self.sentence_train = theano.function(inputs=[cx, x, y, learning_rate],
                                              outputs=nll,
                                              updates=updates)

    def sentence_features(self, context_features, token_features):
        """
        Generate the RNN input from token features and context features
        """
        # context_appended_features = [context_features + windowed_token_feature_vector
        #      for windowed_token_feature_vector in context_window(token_features, self.context_window_size)]
        # return numpy.asarray(context_appended_features).astype(dtype=theano.config.floatX)
        return [numpy.asarray(context_features, dtype=theano.config.floatX),
                numpy.asarray(context_window(token_features, self.context_window_size), dtype=theano.config.floatX)]

    def predict(self, context_features, token_features):
        return self.classify(*self.sentence_features(context_features, token_features))

    def train(self, train_set, valid_set, settings):
        # train with early stopping on validation set

        x_train, y_train = train_set
        x_validate, y_validate = valid_set

        n_training_sentences = len(x_train)

        best_accuracy = -1
        settings['current_learning_rate'] = settings['lr']
        for e in xrange(settings['n_epochs']):
            # print "starting epoch:", e
            # sys.stdout.flush()
            settings['current_epoch'] = e
            tic = time.time()
            nll = 0
            for i in random.sample(range(n_training_sentences), n_training_sentences):
                # print x_train[i]
                utterance_features = self.sentence_features(x_train[i][0], x_train[i][1])
                labels = y_train[i]

                # print "utterance features shape:", utterance_features.shape
                # print "labels:", y_train[i]
                # sys.stdout.flush()
                nll += self.sentence_train(utterance_features[0], utterance_features[1], labels, settings['current_learning_rate'])

            print "epoch:", e, "training set nll:", nll
            sys.stdout.flush()
            predictions_valid = [self.classify(*self.sentence_features(token_features, context_features))
                                 for [token_features, context_features] in x_validate]

            num_truth = Counter()
            num_correct = Counter()
            mean_tag_precisions = []
            for k in range(len(predictions_valid)):
                mtp, nt, nc = evaluate_tagging(predictions_valid[k], y_validate[k])
                num_truth.update(nt)
                num_correct.update(nc)
                mean_tag_precisions.append(mtp)

            # validation_accuracy = numpy.mean(mean_tag_precisions)
            validation_accuracy = numpy.mean([1.0*num_correct[label] / num_truth[label] for label in num_truth.keys()])

            if validation_accuracy > best_accuracy:

                best_accuracy = validation_accuracy
                if settings['verbose']:
                    print 'NEW BEST: epoch', e
                print "validation precision breakdown. Mean:", validation_accuracy
                for key in num_truth.keys():
                    print key, "(", num_correct[key], "/", num_truth[key], ")", 1.0*num_correct[key] / num_truth[key]
                sys.stdout.flush()


                settings['best_epoch'] = e
                f = open(settings['outfile'], 'wb')
                cPickle.dump(self, f, protocol=cPickle.HIGHEST_PROTOCOL)
                f.close()

            # learning rate decay if no improvement in 10 epochs
            if settings['decay'] and abs(settings['best_epoch'] - settings['current_epoch']) >= 5:
                settings['current_learning_rate'] *= 0.5
            if settings['current_learning_rate'] < 1e-5:
                break

        print 'BEST RESULT: epoch', e, 'validation accuracy', best_accuracy


def evaluate_tagging(predictions, ground_truth):
    """
    :param predictions: list of predicted tokens (must be same length as ground_truth)
    :param ground_truth: list of correct tokens
    :return: (mean tag precision, num_truth, num_correct)
    """
    # print "evaluating tagging:"
    # print ground_truth
    # print predictions
    num_truth = Counter()
    num_correct = Counter()
    num_truth.update(ground_truth)
    num_correct.update([predictions[i] for i in range(len(ground_truth)) if predictions[i] == ground_truth[i]])
    return numpy.mean([1.0*num_correct[i] / num_truth[i] for i in num_truth.keys()]), num_truth, num_correct


def shuffle(lol, seed):
    """
    lol :: list of list as input
    seed :: seed the shuffling

    shuffle inplace each list in the same order
    """
    for l in lol:
        random.seed(seed)
        random.shuffle(l)


def mini_batch(l, bs):
    """
    l :: list of items
    return a list of minibatches of indexes
    which size is equal to bs
    border cases are treated as follow:
    eg: [0,1,2,3] and bs = 3
    will output:
    [[0],[0,1],[0,1,2],[1,2,3]]
    """
    out = [l[:i] for i in xrange(1, min(bs, len(l) + 1))]
    out += [l[i - bs:i] for i in xrange(bs, len(l) + 1)]
    assert len(l) == len(out)
    return out


def context_window(token_vector_sequence, win):
    """
    win :: int corresponding to the size of the window
    given a list of indexes composing a sentence
    """
    assert (win % 2) == 1
    assert win >= 1
    token_vector_size = len(token_vector_sequence[0])
    padded_token_sequence = win // 2 * [[0] * token_vector_size] + token_vector_sequence + win // 2 * [
        [0] * token_vector_size]
    # print padded_token_sequence
    out = [reduce(lambda x, y: x + y, padded_token_sequence[i:i + win], []) for i in range(len(token_vector_sequence))]
    assert len(out) == len(token_vector_sequence)
    return out

