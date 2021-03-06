package de.caluga.test.mongo.suite;

import de.caluga.morphium.MorphiumSingleton;
import de.caluga.morphium.query.Query;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Stephan Bösebeck
 * Date: 09.05.12
 * Time: 10:46
 * <p/>
 */
public class UpdateTest extends MongoTest {
    @Test
    public void incMultipleFieldsTest() throws Exception {
        MorphiumSingleton.get().dropCollection(UncachedMultipleCounter.class);
        for (int i = 1; i <= 50; i++) {
            UncachedMultipleCounter o = new UncachedMultipleCounter();
            o.setCounter(i);
            o.setValue("Uncached " + i);
            o.setCounter2((double) i / 2.0);
            MorphiumSingleton.get().store(o);
        }
        Query<UncachedMultipleCounter> q = MorphiumSingleton.get().createQueryFor(UncachedMultipleCounter.class);
        q = q.f("value").eq("Uncached " + 5);

        Map<String, Double> toInc = new HashMap<String, Double>();
        toInc.put("counter", 10.0);
        toInc.put("counter2", 0.5);
        MorphiumSingleton.get().inc(q, toInc, false, true, null);

        assert (q.get().getCounter() == 15) : "counter is:" + q.get().getCounter();
        assert (q.get().getCounter2() == 3);
        MorphiumSingleton.get().inc(q, toInc, false, true, null);
        assert (q.get().getCounter() == 25) : "counter is:" + q.get().getCounter();
        assert (q.get().getCounter2() == 3.5);

    }

    @Test
    public void incTest() throws Exception {
        for (int i = 1; i <= 50; i++) {
            UncachedObject o = new UncachedObject();
            o.setCounter(i);
            o.setValue("Uncached " + i);
            MorphiumSingleton.get().store(o);
        }

        Query<UncachedObject> q = MorphiumSingleton.get().createQueryFor(UncachedObject.class);
        q = q.f("value").eq("Uncached " + 5);
        UncachedObject uc = q.get();
        MorphiumSingleton.get().inc(uc, "counter", 1);

        assert (uc.getCounter() == 6) : "Counter is not correct: " + uc.getCounter();

        //inc without object - single update, no upsert
        q = MorphiumSingleton.get().createQueryFor(UncachedObject.class);
        q = q.f("counter").gte(10).f("counter").lte(25).sort("counter");
        MorphiumSingleton.get().inc(q, "counter", 100);

        uc = q.get();
        assert (uc.getCounter() == 11) : "Counter is wrong: " + uc.getCounter();

        //inc without object directly in DB - multiple update
        q = MorphiumSingleton.get().createQueryFor(UncachedObject.class);
        q = q.f("counter").gt(10).f("counter").lte(25);
        MorphiumSingleton.get().inc(q, "counter", 100, false, true);

        q = MorphiumSingleton.get().createQueryFor(UncachedObject.class);
        q = q.f("counter").gt(110).f("counter").lte(125);
        List<UncachedObject> lst = q.asList(); //read the data after update
        for (UncachedObject u : lst) {
            assert (u.getCounter() > 110 && u.getCounter() <= 125 && u.getValue().equals("Uncached " + (u.getCounter() - 100))) : "Counter wrong: " + u.getCounter();
        }

    }

    @Test
    public void decTest() throws Exception {
        for (int i = 1; i <= 50; i++) {
            UncachedObject o = new UncachedObject();
            o.setCounter(i);
            o.setValue("Uncached " + i);
            MorphiumSingleton.get().store(o);
        }

        Query<UncachedObject> q = MorphiumSingleton.get().createQueryFor(UncachedObject.class);
        q = q.f("value").eq("Uncached " + 5);
        UncachedObject uc = q.get();
        MorphiumSingleton.get().dec(uc, "counter", 1);

        assert (uc.getCounter() == 4) : "Counter is not correct: " + uc.getCounter();

        //inc without object - single update, no upsert
        q = MorphiumSingleton.get().createQueryFor(UncachedObject.class);
        q = q.f("counter").gte(40).f("counter").lte(55).sort("counter");
        MorphiumSingleton.get().dec(q, "counter", 40);

        uc = q.get();
        assert (uc.getCounter() == 41) : "Counter is wrong: " + uc.getCounter();

        //inc without object directly in DB - multiple update
        q = MorphiumSingleton.get().createQueryFor(UncachedObject.class);
        q = q.f("counter").gt(40).f("counter").lte(55);
        MorphiumSingleton.get().dec(q, "counter", 40, false, true);

        q = MorphiumSingleton.get().createQueryFor(UncachedObject.class);
        q = q.f("counter").gt(0).f("counter").lte(55);
        List<UncachedObject> lst = q.asList(); //read the data after update
        for (UncachedObject u : lst) {
            assert (u.getCounter() > 0 && u.getCounter() <= 55) : "Counter wrong: " + u.getCounter();
//            assert(u.getValue().equals("Uncached "+(u.getCounter()-40))):"Value wrong: Counter: "+u.getCounter()+" Value;: "+u.getValue();
        }

    }

    @Test
    public void setTest() throws Exception {
        for (int i = 1; i <= 50; i++) {
            UncachedObject o = new UncachedObject();
            o.setCounter(i);
            o.setValue("Uncached " + i);
            MorphiumSingleton.get().store(o);
        }

        Query<UncachedObject> q = MorphiumSingleton.get().createQueryFor(UncachedObject.class);
        q = q.f("value").eq("unexistent");
        MorphiumSingleton.get().set(q, "counter", 999, true, false);
        UncachedObject uc = q.get(); //should now work

        assert (uc != null) : "Not found?!?!?";
        assert (uc.getValue().equals("unexistent")) : "Value wrong: " + uc.getValue();
    }

    @Test
    public void pushTest() throws Exception {
        MorphiumSingleton.get().dropCollection(ListContainer.class);
        for (int i = 1; i <= 50; i++) {
            ListContainer lc = new ListContainer();
            lc.addLong(12 + i);
            lc.addString("string");
            lc.setName("LC" + i);
            MorphiumSingleton.get().store(lc);
        }

        Query<ListContainer> lc = MorphiumSingleton.get().createQueryFor(ListContainer.class);
        lc = lc.f("name").eq("LC15");
        MorphiumSingleton.get().push(lc, "long_list", 12345l);
        ListContainer cont = lc.get();
        assert (cont.getLongList().contains(12345l)) : "No push?";

    }


    @Test
    public void pushEntityTest() throws Exception {
        MorphiumSingleton.get().dropCollection(ListContainer.class);

        for (int i = 1; i <= 50; i++) {
            ListContainer lc = new ListContainer();
            lc.addLong(12 + i);
            lc.addString("string");
            lc.setName("LC" + i);
            MorphiumSingleton.get().store(lc);
        }
        Query<ListContainer> lc = MorphiumSingleton.get().createQueryFor(ListContainer.class);
        lc = lc.f("name").eq("LC15");
        EmbeddedObject em = new EmbeddedObject();
        em.setValue("emb Value");
        em.setTest(1);
        MorphiumSingleton.get().push(lc, "embedded_object_list", em);
        em = new EmbeddedObject();
        em.setValue("emb Value 2");
        em.setTest(2);
        MorphiumSingleton.get().push(lc, "embedded_object_list", em);
        waitForWrites();

        ListContainer lc2 = lc.get();
        assert (lc2.getEmbeddedObjectList() != null);
        assert (lc2.getEmbeddedObjectList().size() == 2);
        assert (lc2.getEmbeddedObjectList().get(0).getTest() == 1l);
    }

    @Test
    public void unsetTest() throws Exception {
        createUncachedObjects(100);
        Query<UncachedObject> q = MorphiumSingleton.get().createQueryFor(UncachedObject.class).f("counter").eq(50);
        MorphiumSingleton.get().unsetQ(q, "value");
        UncachedObject uc = q.get();
        assert (uc.getValue() == null);
        q = MorphiumSingleton.get().createQueryFor(UncachedObject.class).f("counter").gt(90);
        MorphiumSingleton.get().unsetQ(q, false, "value");
        List<UncachedObject> lst = q.asList();
        boolean found = false;
        for (UncachedObject u : lst) {
            if (u.getValue() == null) {
                assert (!found);
                found = true;
            }
        }
        assert (found);
        MorphiumSingleton.get().unsetQ(q, true, "binary_data", "bool_data", "value");
        lst = q.asList();
        for (UncachedObject u : lst) {
            assert (u.getValue() == null);
        }

    }

    @Test
    public void pushEntityListTest() throws Exception {
        MorphiumSingleton.get().dropCollection(ListContainer.class);

        for (int i = 1; i <= 50; i++) {
            ListContainer lc = new ListContainer();
            lc.addLong(12 + i);
            lc.addString("string");
            lc.setName("LC" + i);
            MorphiumSingleton.get().store(lc);
        }

        List<EmbeddedObject> obj = new ArrayList<EmbeddedObject>();

        Query<ListContainer> lc = MorphiumSingleton.get().createQueryFor(ListContainer.class);
        lc = lc.f("name").eq("LC15");
        EmbeddedObject em = new EmbeddedObject();
        em.setValue("emb Value");
        em.setTest(1);
        obj.add(em);

        em = new EmbeddedObject();
        em.setValue("emb Value 2");
        em.setTest(2);
        obj.add(em);

        em = new EmbeddedObject();
        em.setValue("emb Value 3");
        em.setTest(3);
        obj.add(em);


        MorphiumSingleton.get().pushAll(lc, "embedded_object_list", obj, false, true);
        waitForWrites();
        Thread.sleep(2500);
        ListContainer lc2 = lc.get();
        assert (lc2.getEmbeddedObjectList() != null);
        assert (lc2.getEmbeddedObjectList().size() == 3) : "Size wrong should be 3 is " + lc2.getEmbeddedObjectList().size();
        assert (lc2.getEmbeddedObjectList().get(0).getTest() == 1l);
    }

    public static class UncachedMultipleCounter extends UncachedObject {
        private double counter2;

        public double getCounter2() {
            return counter2;
        }

        public void setCounter2(double counter2) {
            this.counter2 = counter2;
        }
    }

}
