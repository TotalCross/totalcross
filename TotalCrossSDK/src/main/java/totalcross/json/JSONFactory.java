// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.json;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The JSONFactory class helps converting json objects into Java objects, using
 * reflection.
 * 
 * Some examples:
 * 
 * <pre>
 * class Car {
 *   private int id;
 *   private String description;
 * 
 *   public int getId() {
 *     return id;
 *   }
 * 
 *   public void setId(int id) {
 *     this.id = id;
 *   }
 * 
 *   public String getDescription() {
 *     return description;
 *   }
 * 
 *   public void setDescription(String description) {
 *     this.description = description;
 *   }
 * }
 * </pre>
 * 
 * You can retrieve a new Car object using:
 * 
 * <pre>
 * Car cc = JSONFactory.parse("{\"carro\":{\"id\":-1,\"descricao\":\"GOL\"}}", Carro.class);
 * </pre>
 * 
 * You may also retrieve a list or an array. See the JSONSample in the
 * TotalCrossAPI.
 */
public class JSONFactory {
  private static Map<Class<?>, Map<String, Method>> classes = new HashMap<>();

  public static <T> List<T> asList(String json, Class<T> classOfT)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      JSONException, ArrayIndexOutOfBoundsException, NoSuchMethodException, SecurityException {
    List<T> list = new ArrayList<T>();
    try {
      JSONArray jsonArray = new JSONArray(json);
      for (int i = 0; i < jsonArray.length(); i++) {
        try {
          list.add(parse(jsonArray.getJSONObject(i), classOfT));
        } catch (JSONException e) {
          list.add(parse(jsonArray.getJSONArray(i), classOfT));
        }
      }
    } catch (JSONException e) {
      list.add(parse(json, classOfT));
    }

    return list;
  }

  public static <T> T parse(String json, Class<T> classOfT) throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, JSONException, NoSuchMethodException, SecurityException {
    if (classOfT.isArray()) {
      T array;
      try {
        JSONArray jsonArray = new JSONArray(json);
        array = classOfT.cast(Array.newInstance(classOfT.getComponentType(), jsonArray.length()));
        for (int i = jsonArray.length() - 1; i >= 0; i--) {
          Array.set(array, i, parse(jsonArray.getJSONObject(i), classOfT.getComponentType()));
        }
      } catch (JSONException e) {
        array = classOfT.cast(Array.newInstance(classOfT.getComponentType(), 1));
        Array.set(array, 0, parse(new JSONObject(json), classOfT.getComponentType()));
      }
      return array;
    }
    return parse(new JSONObject(json), classOfT);
  }

  public static <T> T parse(JSONArray jsonArray, Class<T> classOfT)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      JSONException, ArrayIndexOutOfBoundsException, NoSuchMethodException, SecurityException {
    return parse(null, jsonArray, classOfT);
  }

  private static <T> T parse(Object outerObject, JSONArray jsonArray, Class<T> classOfT)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      JSONException, ArrayIndexOutOfBoundsException, NoSuchMethodException, SecurityException {
    if (classOfT.isArray()) {
      T array;
      try {
        array = classOfT.cast(Array.newInstance(classOfT.getComponentType(), jsonArray.length()));
        for (int i = jsonArray.length() - 1; i >= 0; i--) {
          Array.set(array, i, parse(outerObject, jsonArray.getJSONObject(i), classOfT.getComponentType()));
        }
      } catch (JSONException e) {
        array = classOfT.cast(Array.newInstance(classOfT.getComponentType(), 1));
        Array.set(array, 0, parse(outerObject, jsonArray, classOfT.getComponentType()));
      }
      return array;
    }
    return parse(outerObject, jsonArray, classOfT);
  }

  public static <T> T parse(JSONObject jsonObject, Class<T> classOfT)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      JSONException, NoSuchMethodException, SecurityException {
    return parse(null, jsonObject, classOfT);
  }

  private static <T> T parse(Object outerObject, JSONObject jsonObject, Class<T> classOfT)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      JSONException, NoSuchMethodException, SecurityException {
    if (classOfT.isArray()) {
      throw new IllegalArgumentException();
    }
    T object = null;
    try {
      object = classOfT.newInstance();
    } catch (InstantiationException e) {
      if (outerObject != null && classOfT.getName().indexOf(outerObject.getClass().getName()) != -1) {
        Constructor<T> constructorOfT = classOfT.getDeclaredConstructor(outerObject.getClass());
        if (constructorOfT != null) {
          object = constructorOfT.newInstance(outerObject);
        }
      }
      if (object == null) {
        throw e;
      }
    }

    Map<String, Method> methodCache = classes.get(classOfT);
    if (methodCache == null) {
      methodCache = mapClassMethodsToJsonNames(classOfT);
      classes.put(classOfT, methodCache);
    }

    Iterator<String> keyIterator = jsonObject.keys();
    while (keyIterator.hasNext()) {
      final String name = keyIterator.next();
      Method method = methodCache.get(name);
      if (method == null || jsonObject.isNull(name)) {
        continue;
      }

      Class<?> parameterType = method.getParameterTypes()[0];
      if (parameterType.isPrimitive()) {
        if (parameterType.isAssignableFrom(boolean.class)) {
          method.invoke(object, jsonObject.getBoolean(name));
        } else if (parameterType.isAssignableFrom(int.class)) {
          method.invoke(object, jsonObject.getInt(name));
        } else if (parameterType.isAssignableFrom(long.class)) {
          method.invoke(object, jsonObject.getLong(name));
        } else if (parameterType.isAssignableFrom(double.class)) {
          method.invoke(object, jsonObject.getDouble(name));
        }
      } else if (parameterType.isAssignableFrom(String.class)) {
        method.invoke(object, jsonObject.getString(name));
      } else if (parameterType.isAssignableFrom(Double.class)) {
        method.invoke(object, jsonObject.getDouble(name));
      } else if (parameterType.isAssignableFrom(Integer.class)) {
        method.invoke(object, jsonObject.getInt(name));
      } else if (parameterType.isAssignableFrom(Long.class)) {
        method.invoke(object, jsonObject.getLong(name));
      } else if (parameterType.isAssignableFrom(Boolean.class)) {
        method.invoke(object, jsonObject.getBoolean(name));
      } else if (parameterType.isArray()) {
        method.invoke(object, parse(object, jsonObject.getJSONArray(name), parameterType));
      } else {
        method.invoke(object, parse(object, jsonObject.getJSONObject(name), parameterType));
      }
    }
    return object;
  }

  private static Map<String, Method> mapClassMethodsToJsonNames(Class<?> classOfT) {
    final Map<String, Method> methodCache = new HashMap<>();
    Method[] methods = classOfT.getMethods();
    for (Method method : methods) {
      String methodName = method.getName();
      Class<?>[] paramTypes = method.getParameterTypes();
      if (paramTypes != null && paramTypes.length == 1 && methodName.length() > 3 && methodName.startsWith("set")) {
        final String originalName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        // name as-is
        methodCache.put(originalName, method);
        // lowercased name
        methodCache.put(originalName.toLowerCase(), method);
        // not found as-is or lowercased? try replacing camel case with underscore
        /*
         * originally done using regex, but totalcross implementation has some bugs and
         * until they are fixed this is done looping through the characters
         * originalName.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
         */
        StringBuilder sb = new StringBuilder();
        boolean lastWasUnderscored = false;
        for (int i = 0; i < originalName.length(); i++) {
          char c = originalName.charAt(i);
          if (!lastWasUnderscored && Character.isUpperCase(c)) {
            lastWasUnderscored = true;
            sb.append('_');
          } else {
            lastWasUnderscored = false;
          }
          sb.append(Character.toLowerCase(c));
        }
        final String underscoredName = sb.toString();
        methodCache.put(underscoredName, method);
      }
    }
    return methodCache;
  }
}
