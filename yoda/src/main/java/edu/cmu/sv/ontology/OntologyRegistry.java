package edu.cmu.sv.ontology;

import edu.cmu.sv.ontology.adjective.*;
import edu.cmu.sv.ontology.misc.*;
import edu.cmu.sv.ontology.noun.*;
import edu.cmu.sv.ontology.noun.poi_types.*;
import edu.cmu.sv.ontology.preposition.IsCloseTo;
import edu.cmu.sv.ontology.preposition.Preposition;
import edu.cmu.sv.ontology.quality.TransientQuality;
import edu.cmu.sv.ontology.quality.binary_quality.Distance;
import edu.cmu.sv.ontology.quality.unary_quality.Expensiveness;
import edu.cmu.sv.ontology.quality.unary_quality.Goodness;
import edu.cmu.sv.ontology.quality.unary_quality.Popularity;
import edu.cmu.sv.ontology.role.*;
import edu.cmu.sv.ontology.role.has_quality_subroles.*;
import edu.cmu.sv.ontology.verb.*;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by David Cohen on 9/22/14.
 */
public class OntologyRegistry {
    public static Set<Class <? extends Verb>> verbClasses = new HashSet<>();
    public static Set<Class <? extends Noun>> nounClasses = new HashSet<>();
    public static Set<Class <? extends Adjective>> adjectiveClasses = new HashSet<>();
    public static Set<Class <? extends Preposition>> prepositionClasses = new HashSet<>();
    public static Set<Class <? extends Role>> roleClasses = new HashSet<>();
    public static Set<Class <? extends TransientQuality>> qualityClasses = new HashSet<>();
    public static Set<Class <? extends Thing>> miscClasses = new HashSet<>();

    public static Map<String, Class <? extends Thing>> thingNameMap = new HashMap<>();
    public static Map<String, Class <? extends Verb>> verbNameMap = new HashMap<>();
    public static Map<String, Class <? extends Role>> roleNameMap = new HashMap<>();
    public static Map<String, Thing> individualNameMap = new HashMap<>();

    public static Map<Class<? extends ThingWithRoles>, Set<Class<? extends TransientQuality>>> qualitiesForClass = new HashMap<>();

    static{
        // register classes
        verbClasses.add(Verb.class);
        verbClasses.add(Create.class);
        verbClasses.add(HasProperty.class);
        verbClasses.add(Exist.class);
        verbClasses.add(GiveDirections.class);
        verbClasses.add(MakeReservation.class);

        nounClasses.add(Noun.class);
        nounClasses.add(Person.class);
        nounClasses.add(Email.class);
        nounClasses.add(Meeting.class);
        nounClasses.add(Time.class);
        nounClasses.add(PointOfInterest.class);

	nounClasses.add(Restaurants.class);
	nounClasses.add(Food.class);
	nounClasses.add(Bars.class);
	nounClasses.add(Mexican.class);
	nounClasses.add(AmericanTraditional.class);
	nounClasses.add(FastFood.class);
	nounClasses.add(Pizza.class);
	nounClasses.add(HotelsAndTravel.class);
	nounClasses.add(Sandwiches.class);
	nounClasses.add(CoffeeAndTea.class);
	nounClasses.add(AmericanNew.class);
	nounClasses.add(Italian.class);
	nounClasses.add(Chinese.class);
	nounClasses.add(Hotels.class);
	nounClasses.add(Burgers.class);
	nounClasses.add(Grocery.class);
	nounClasses.add(BreakfastAndBrunch.class);
	nounClasses.add(IceCreamAndFrozenYogurt.class);
	nounClasses.add(SpecialtyFood.class);
	nounClasses.add(Bakeries.class);
	nounClasses.add(Pubs.class);
	nounClasses.add(Japanese.class);
	nounClasses.add(SportsBars.class);
	nounClasses.add(ConvenienceStores.class);
	nounClasses.add(Delis.class);
	nounClasses.add(SushiBars.class);
	nounClasses.add(Steakhouses.class);
	nounClasses.add(Cafes.class);
	nounClasses.add(Seafood.class);
	nounClasses.add(Desserts.class);
	nounClasses.add(Buffets.class);
	nounClasses.add(Barbeque.class);
	nounClasses.add(Thai.class);
	nounClasses.add(Mediterranean.class);
	nounClasses.add(BeerWineAndSpirits.class);
	nounClasses.add(ChickenWings.class);
	nounClasses.add(AsianFusion.class);
	nounClasses.add(JuiceBarsAndSmoothies.class);
	nounClasses.add(Greek.class);
	nounClasses.add(Indian.class);
	nounClasses.add(TexMex.class);
	nounClasses.add(Donuts.class);
	nounClasses.add(Diners.class);
	nounClasses.add(HotDogs.class);
	nounClasses.add(Vietnamese.class);
	nounClasses.add(WineBars.class);
	nounClasses.add(LocalFlavor.class);
	nounClasses.add(Salad.class);
	nounClasses.add(DiveBars.class);
	nounClasses.add(Vegetarian.class);
	nounClasses.add(British.class);
	nounClasses.add(French.class);
	nounClasses.add(Bagels.class);
	nounClasses.add(Korean.class);
	nounClasses.add(EthnicFood.class);
	nounClasses.add(Hawaiian.class);
	nounClasses.add(Caterers.class);
	nounClasses.add(GlutenFree.class);
	nounClasses.add(MiddleEastern.class);
	nounClasses.add(FarmersMarket.class);
	nounClasses.add(Gastropubs.class);
	nounClasses.add(LatinAmerican.class);
	nounClasses.add(FoodTrucks.class);
	nounClasses.add(Karaoke.class);
	nounClasses.add(CandyStores.class);
	nounClasses.add(Breweries.class);
	nounClasses.add(FishAndChips.class);
	nounClasses.add(Vegan.class);
	nounClasses.add(GayBars.class);
	nounClasses.add(ChocolatiersAndShops.class);
	nounClasses.add(FoodDeliveryServices.class);
	nounClasses.add(Pakistani.class);
	nounClasses.add(ShavedIce.class);
	nounClasses.add(FoodStands.class);
	nounClasses.add(Filipino.class);
	nounClasses.add(CocktailBars.class);
	nounClasses.add(Southern.class);
	nounClasses.add(HookahBars.class);
	nounClasses.add(CajunCreole.class);
	nounClasses.add(Irish.class);
	nounClasses.add(TeaRooms.class);
	nounClasses.add(SoulFood.class);
	nounClasses.add(Soup.class);
	nounClasses.add(Caribbean.class);
	nounClasses.add(Spanish.class);
	nounClasses.add(TapasSmallPlates.class);
	nounClasses.add(FruitsAndVeggies.class);
	nounClasses.add(Cheesesteaks.class);
	nounClasses.add(TapasBars.class);
	nounClasses.add(SportsClubs.class);
	nounClasses.add(DimSum.class);
	nounClasses.add(ComfortFood.class);
	nounClasses.add(ModernEuropean.class);
	nounClasses.add(Scottish.class);
	nounClasses.add(Creperies.class);
	nounClasses.add(CheeseShops.class);

        // nounClasses.add(Bank.class);
        // nounClasses.add(Bar.class);
        // nounClasses.add(Bench.class);
        // nounClasses.add(BicycleParking.class);
        // nounClasses.add(Cafe.class);
        // nounClasses.add(FastFood.class);
        // nounClasses.add(GarbageCan.class);
        // nounClasses.add(GasStation.class);
        // nounClasses.add(GraveYard.class);
        // nounClasses.add(Hospital.class);
        // nounClasses.add(Kindergarten.class);
        // nounClasses.add(MailBox.class);
        // nounClasses.add(Parking.class);
        // nounClasses.add(Pharmacy.class);
        // nounClasses.add(PlaceOfWorship.class);
        // nounClasses.add(PostOffice.class);
        // nounClasses.add(PublicBuilding.class);
        // nounClasses.add(PublicTelephone.class);
        // nounClasses.add(Recycling.class);
        // nounClasses.add(Restaurant.class);
        // nounClasses.add(Restroom.class);
        // nounClasses.add(School.class);
        // nounClasses.add(Shelter.class);

        roleClasses.add(Role.class);
        roleClasses.add(Agent.class);
        roleClasses.add(Patient.class);
        roleClasses.add(HasAtTime.class);
        roleClasses.add(HasHour.class);
        roleClasses.add(HasName.class);
        roleClasses.add(HasValues.class);
        roleClasses.add(HasValue.class);
        roleClasses.add(HasURI.class);
        roleClasses.add(HasDistance.class);
        roleClasses.add(HasExpensiveness.class);
        roleClasses.add(HasGoodness.class);
        roleClasses.add(HasPopularity.class);
        roleClasses.add(InRelationTo.class);
        roleClasses.add(Origin.class);
        roleClasses.add(Destination.class);

        adjectiveClasses.add(Cheap.class);
        adjectiveClasses.add(Expensive.class);
        adjectiveClasses.add(Good.class);
        adjectiveClasses.add(Popular.class);
//        adjectiveClasses.add(ExpensivenessAdjective.class);

        prepositionClasses.add(IsCloseTo.class);
//        prepositionClasses.add(DistancePreposition.class);

        qualityClasses.add(Expensiveness.class);
        qualityClasses.add(Goodness.class);
        qualityClasses.add(Popularity.class);
        qualityClasses.add(Distance.class);

        miscClasses.add(NonHearing.class);
        miscClasses.add(NonUnderstanding.class);
        miscClasses.add(Requested.class);
        miscClasses.add(Suggested.class);
        miscClasses.add(UnknownThingWithRoles.class);
        miscClasses.add(Or.class);
        miscClasses.add(And.class);
        miscClasses.add(WebResource.class);

        // recursively register parents
        recursivelyRegisterParents(verbClasses);
        recursivelyRegisterParents(nounClasses);
        recursivelyRegisterParents(roleClasses);
//        recursivelyRegisterParents(qualityClasses);
        recursivelyRegisterParents(miscClasses);

        // register individuals

        // add ubiquitous Things to domains / ranges
        for (Class <? extends ThingWithRoles> cls : Arrays.asList(UnknownThingWithRoles.class)){
            for (Class <? extends Role> roleCls : roleClasses){
                if (roleCls==Role.class)
                    continue;
                try {
                    roleCls.newInstance().getDomain().add(cls);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        // get name maps
        addToNameMap(verbNameMap, verbClasses);
        addToNameMap(roleNameMap, roleClasses);
        addToNameMap(thingNameMap, verbClasses);
        addToNameMap(thingNameMap, nounClasses);
        addToNameMap(thingNameMap, adjectiveClasses);
        addToNameMap(thingNameMap, prepositionClasses);
        addToNameMap(thingNameMap, roleClasses);
        addToNameMap(thingNameMap, miscClasses);
        addToNameMap(thingNameMap, qualityClasses);

        // register qualities for class
        for (Class<? extends TransientQuality> qualityClass : qualityClasses){
            Set<Class<? extends ThingWithRoles>> qualityDom = qualityDomain(qualityClass);
            for (Class<? extends ThingWithRoles> thingCls : qualityDom){
                if (!qualitiesForClass.containsKey(thingCls))
                    qualitiesForClass.put(thingCls, new HashSet<>());
                qualitiesForClass.get(thingCls).add(qualityClass);
            }
        }
    }


    /*
    * Return the set of ThingWithRole classes that can be described by the given quality class
    * */
    public static Set<Class< ? extends ThingWithRoles>> qualityDomain(Class<? extends TransientQuality> qualityClass){
        Pair<Class<? extends Role>, Set<Class<? extends ThingWithRoles>>> qualityDescriptor = qualityDescriptors(qualityClass);
        return thingNameMap.values().stream().
                filter(x -> !Modifier.isAbstract(x.getModifiers())).
                filter(ThingWithRoles.class::isAssignableFrom).
                filter(x -> inDomain(qualityDescriptor.getKey(), (Class<? extends ThingWithRoles>) x)).
                map(x -> (Class<? extends ThingWithRoles>) x).
                collect(Collectors.toSet());
    }

    /*
    * Return the List of Thing classes that are arguments of the given quality
    * */
    public static List<Class< ? extends Thing>> qualityArguments(Class<? extends TransientQuality> qualityClass){
        try {
            return qualityClass.newInstance().getArguments();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new Error("can't determine the quality class's arguments");
    }

    /*
    * Returns the HasQualityRole, and the set of quality descriptors (Adjectives + Prepositions)
    * corresponding to a given quality class
    * */
    public static Pair<Class<? extends Role>, Set<Class<? extends ThingWithRoles>>> qualityDescriptors(
            Class<? extends TransientQuality> qualityClass){
        if (Modifier.isAbstract(qualityClass.getModifiers()))
            return null;
        try {
            Set<Class<? extends ThingWithRoles>> adjectiveAndPrepositionClasses = new HashSet<>();
            for (Class<? extends Adjective> cls : adjectiveClasses){
                if (cls.newInstance().getQuality().equals(qualityClass)) {
                    adjectiveAndPrepositionClasses.add(cls);
                }
            }
            for (Class<? extends Preposition> cls : prepositionClasses){
                if (cls.newInstance().getQuality().equals(qualityClass)){
                    adjectiveAndPrepositionClasses.add(cls);
                }
            }
            Class<? extends Role> roleClass = roleClasses.stream().
                    filter(HasQualityRole.class::isAssignableFrom).
                    filter(x -> inRange(x, new LinkedList<>(adjectiveAndPrepositionClasses).get(0))).
                    collect(Collectors.toList()).get(0);
            return new ImmutablePair<>(roleClass, adjectiveAndPrepositionClasses);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new Error("OntologyRegistry.qualityDescriptors: instantiation or access exception:" + qualityClass);
        }
    }

    public static boolean inDomain(Class<? extends Role> roleClass, Class<? extends ThingWithRoles> subjectClass){
        try {
            if (Modifier.isAbstract(roleClass.getModifiers()))
                return false;
            return roleClass.newInstance().getDomain().stream().anyMatch(x -> x.isAssignableFrom(subjectClass));
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new Error("inDomain:failed to init object");
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean inRange(Class<? extends Role> roleClass , Class<? extends Thing> objectClass){
        try {
            if (Modifier.isAbstract(roleClass.getModifiers()))
                return false;
            return roleClass.newInstance().getRange().stream().anyMatch(x -> x.isAssignableFrom(objectClass));
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new Error("inRange:failed to init object"+objectClass);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static <S,T> void addToNameMap(Map<String, Class <? extends S>> nameMap, Set<Class <? extends T>> classSet){
        for (Class <? extends T> cls : classSet){
            String id = cls.getSimpleName();
            if (nameMap.keySet().contains(id)){
                throw new ValueException("NAMING CONFLICT: The class name:"+id+" is already registered.");
            }
            nameMap.put(id, (Class<? extends S>) cls);
        }
    }

    public static <T> void recursivelyRegisterParents(Set<Class <? extends T>> classSet){
        List<Class> queue = new LinkedList<>(classSet);
        while (!queue.isEmpty()) {
            Class cls = queue.get(0);
            queue.remove(cls);
            Class<? extends T> superCls = cls.getSuperclass();
            if (!Modifier.isAbstract(superCls.getModifiers())) {
                classSet.add(superCls);
                queue.add(superCls);
            }
        }
    }

    public static String webResourceWrap(String URI){
        String ans = "{\"class\": \""+ WebResource.class.getSimpleName()+"\", \""+
                HasURI.class.getSimpleName()+"\":\""+URI+"\"}";
        return ans;
    }

}
