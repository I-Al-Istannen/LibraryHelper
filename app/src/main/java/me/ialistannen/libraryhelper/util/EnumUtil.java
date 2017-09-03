package me.ialistannen.libraryhelper.util;

import com.google.common.base.Function;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Some static utility functions for enums.
 */
public class EnumUtil {

  /**
   * @param enumClass The class of the enum
   * @param transformation The transformation to apply
   * @param <T> The enum class
   * @param <V> The class of the transformed value
   * @return A reverse-mapping of the enum
   */
  public static <T extends Enum<T>, V> Map<V, T> getReverseMapping(Class<T> enumClass,
      Function<T, V> transformation) {

    Map<V, T> result = new HashMap<>();

    for (T t : enumClass.getEnumConstants()) {
      result.put(transformation.apply(t), t);
    }

    return result;
  }

  /**
   * @param <T> The enum class
   * @param <V> The class of the transformed value
   * @param enumClass The class of the enum
   * @param transformation The transformation to apply
   * @return A List with all enum entries transformed
   */
  public static <T extends Enum<T>, V> List<V> transformEnum(Class<T> enumClass,
      Function<T, V> transformation) {

    List<V> result = new ArrayList<>();

    for (T t : enumClass.getEnumConstants()) {
      result.add(transformation.apply(t));
    }

    return result;
  }
}
