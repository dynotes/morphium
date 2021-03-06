package de.caluga.test.mongo.suite;

import com.mongodb.DBObject;
import de.caluga.morphium.MorphiumSingleton;
import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.annotations.Id;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by stephan on 18.11.14.
 */
public class NonEntitySerialization extends MongoTest {

    @Test
    public void testNonEntity() throws Exception {
        NonEntity ne = new NonEntity();
        ne.setInteger(42);
        ne.setValue("Thank you for the fish");

        DBObject obj = MorphiumSingleton.get().getMapper().marshall(ne);
        log.info(obj.toString());

        NonEntity ne2 = MorphiumSingleton.get().getMapper().unmarshall(NonEntity.class, obj);
        assert (ne2.getInteger() == 42);
        log.info("Successful read:" + ne2.toString());
    }

    @Test
    public void testNonEntityList() throws Exception {
        NonEntityContainer nc = new NonEntityContainer();
        nc.setList(new ArrayList<Object>());
        NonEntity ne = new NonEntity();
        ne.setInteger(42);
        ne.setValue("Thank you for the fish");

        nc.getList().add(ne);
        nc.getList().add("Some string");

        DBObject obj = MorphiumSingleton.get().getMapper().marshall(nc);

        NonEntityContainer nc2 = MorphiumSingleton.get().getMapper().unmarshall(NonEntityContainer.class, obj);
        assert (nc2.getList().get(0) != null);
        NonEntity ne2 = (NonEntity) nc2.getList().get(0);
        assert (ne2.getInteger() == 42);

        //now store to Mongo
        MorphiumSingleton.get().dropCollection(NonEntityContainer.class);
        MorphiumSingleton.get().store(nc);

        Thread.sleep(1000);

        nc2 = MorphiumSingleton.get().findById(NonEntityContainer.class, nc.getId());
        assert (nc2.getList().get(0) != null);
        ne2 = (NonEntity) nc2.getList().get(0);
        assert (ne2.getInteger() == 42);
        assert (nc2.getList().get(1).equals("Some string")) : "Wrong Value: " + nc2.getList().get(1);
    }

    @Test
    public void testNonEntityMap() throws Exception {
        NonEntityContainer nc = new NonEntityContainer();

        nc.setMap(new HashMap<String, Object>());

        NonEntity ne = new NonEntity();
        ne.setInteger(42);
        ne.setValue("Thank you for the fish");

        nc.getMap().put("Serialized", ne);
        nc.getMap().put("String", "The question is...");


        DBObject obj = MorphiumSingleton.get().getMapper().marshall(nc);

        NonEntityContainer nc2 = MorphiumSingleton.get().getMapper().unmarshall(NonEntityContainer.class, obj);
        assert (nc2.getMap().get("Serialized") != null);
        NonEntity ne2 = (NonEntity) nc2.getMap().get("Serialized");
        assert (ne2.getInteger() == 42);

        //now store to Mongo
        MorphiumSingleton.get().dropCollection(NonEntityContainer.class);
        MorphiumSingleton.get().store(nc);

        Thread.sleep(1000);

        nc2 = MorphiumSingleton.get().findById(NonEntityContainer.class, nc.getId());
        assert (nc2.getMap().get("Serialized") != null);
        ne2 = (NonEntity) nc2.getMap().get("Serialized");
        assert (ne2.getInteger() == 42);
    }


    @Entity
    public static class NonEntityContainer {
        @Id
        private ObjectId id;
        private List<Object> list;
        private HashMap<String, Object> map;

        public ObjectId getId() {
            return id;
        }

        public void setId(ObjectId id) {
            this.id = id;
        }

        public List<Object> getList() {
            return list;
        }

        public void setList(List<Object> list) {
            this.list = list;
        }

        public HashMap<String, Object> getMap() {
            return map;
        }

        public void setMap(HashMap<String, Object> map) {
            this.map = map;
        }
    }


    public static class NonEntity implements Serializable {
        private String value;
        private Integer integer;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Integer getInteger() {
            return integer;
        }

        public void setInteger(Integer integer) {
            this.integer = integer;
        }

        @Override
        public String toString() {
            return "NonEntity{" +
                    "value='" + value + '\'' +
                    ", integer=" + integer +
                    '}';
        }
    }
}
