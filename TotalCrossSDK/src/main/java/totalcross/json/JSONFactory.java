package totalcross.json;

import java.lang.reflect.*;
import java.util.*;

/**
    The JSONFactory class helps converting json objects into Java objects, using reflection.
    
    Some examples:
    
    <pre>
    class Car 
    {
       private int id;
       private String description;
       
       public int getId()
       {
          return id;
       }
       public void setId(int id)
       {
          this.id = id;
       }
       public String getDescription()
       {
          return description;
       }
       public void setDescription(String description)
       {
          this.description = description;
       }
    }
    </pre>
    
    You can retrieve a new Car object using:
    <pre>
    Car cc = JSONFactory.parse("{\"carro\":{\"id\":-1,\"descricao\":\"GOL\"}}", Carro.class);
    </pre>
    
    You may also retrieve a list or an array. See the JSONSample in the TotalCrossAPI.
 */
public class JSONFactory 
{
   public static <T> List<T> asList(String json, Class<T> classOfT) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException
   {
      List<T> list = new ArrayList<T>();
      try
      {
         JSONArray jsonArray = new JSONArray(json);
         for (int i = 0; i < jsonArray.length(); i++)
         {
            try
            {
               list.add(parse(jsonArray.getJSONObject(i), classOfT));
            }
            catch (JSONException e)
            {
               list.add(parse(jsonArray.getJSONArray(i), classOfT));
            }
         }
      }
      catch (JSONException e)
      {
         list.add(parse(json, classOfT));
      }

      return list;
   }

   public static <T> T parse(String json, Class<T> classOfT) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException
   {
      if (classOfT.isArray())
      {
         T array;
         try
         {
            JSONArray jsonArray = new JSONArray(json);
            array = classOfT.cast(Array.newInstance(classOfT.getComponentType(), jsonArray.length()));
            for (int i = jsonArray.length() - 1; i >= 0; i--)
            {
               Array.set(array, i, parse(jsonArray.getJSONObject(i), classOfT.getComponentType()));
            }
         }
         catch (JSONException e)
         {
            array = classOfT.cast(Array.newInstance(classOfT.getComponentType(), 1));
            Array.set(array, 0, parse(new JSONObject(json), classOfT.getComponentType()));
         }
         return array;
      }
      return parse(new JSONObject(json), classOfT);
   }

   public static <T> T parse(JSONArray jsonArray, Class<T> classOfT) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException
   {
      if (classOfT.isArray())
      {
         T array;
         try
         {
            array = classOfT.cast(Array.newInstance(classOfT.getComponentType(), jsonArray.length()));
            for (int i = jsonArray.length() - 1; i >= 0; i--)
            {
               Array.set(array, i, parse(jsonArray.getJSONObject(i), classOfT.getComponentType()));
            }
         }
         catch (JSONException e)
         {
            array = classOfT.cast(Array.newInstance(classOfT.getComponentType(), 1));
            Array.set(array, 0, parse(jsonArray, classOfT.getComponentType()));
         }
         return array;
      }
      return parse(jsonArray, classOfT);
   }

   public static <T> T parse(JSONObject jsonObject, Class<T> classOfT) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException
   {
      if (classOfT.isArray()) { throw new IllegalArgumentException(); }
      T object = classOfT.newInstance();
      Method[] methods = classOfT.getMethods();
      for (Method method : methods)
      {
         String methodName = method.getName();
         Class<?>[] paramTypes = method.getParameterTypes();
         if (paramTypes != null && paramTypes.length == 1 && methodName.length() > 3 && methodName.startsWith("set"))
         {
            String name = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            if ((jsonObject.isNull(name)))
               name = name.toLowerCase();
            if (!jsonObject.isNull(name))
            {
               Class<?> parameterType = method.getParameterTypes()[0];
               if (parameterType.isPrimitive())
               {
                  if (parameterType.isAssignableFrom(boolean.class))
                     method.invoke(object, jsonObject.getBoolean(name));
                  else
                  if (parameterType.isAssignableFrom(int.class))
                     method.invoke(object, jsonObject.getInt(name));
                  else
                  if (parameterType.isAssignableFrom(long.class))
                     method.invoke(object, jsonObject.getLong(name));
                  else
                  if (parameterType.isAssignableFrom(double.class))
                     method.invoke(object, jsonObject.getDouble(name));
               }
               else
               if (parameterType.isAssignableFrom(String.class))
                  method.invoke(object, jsonObject.getString(name));
               else 
               if (parameterType.isAssignableFrom(Double.class))
                  method.invoke(object, jsonObject.getDouble(name));
               else 
               if (parameterType.isAssignableFrom(Integer.class))
                  method.invoke(object, jsonObject.getInt(name));
               else 
               if (parameterType.isAssignableFrom(Long.class))
                  method.invoke(object, jsonObject.getLong(name));
               else 
               if (parameterType.isAssignableFrom(Boolean.class))
                  method.invoke(object, jsonObject.getBoolean(name));
               else
                  method.invoke(object, parse(jsonObject.getJSONObject(name), parameterType.getClass()));
            }
         }
      }
      return object;
   }
}
