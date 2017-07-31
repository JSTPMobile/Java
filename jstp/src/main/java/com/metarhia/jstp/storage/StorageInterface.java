package com.metarhia.jstp.storage;

import java.io.Serializable;

/**
 * Storage interface for {@link com.metarhia.jstp.connection.SessionPolicy}
 */
public interface StorageInterface {

  /**
   * Puts value into storage
   *
   * @param key   key of stored value
   * @param value serializable value to store
   */
  void putSerializable(String key, Serializable value);

  /**
   * Gets value from storage
   *
   * @param key          key of stored value
   * @param defaultValue default value to return if nothing is found
   *
   * @return stored value
   */
  Object getSerializable(String key, Object defaultValue);
}
