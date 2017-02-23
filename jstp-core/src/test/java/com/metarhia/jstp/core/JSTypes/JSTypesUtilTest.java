package com.metarhia.jstp.core.JSTypes;

import static org.junit.Assert.assertEquals;

import com.metarhia.jstp.core.JSParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * Created by lundibundi on 8/24/16.
 */
public class JSTypesUtilTest {

  @Test
  public void JSToJavaNumber() throws Exception {
    double value = 4.66;
    double actual = (double) JSTypesUtil.jsToJava(new JSNumber(value));

    assertEquals(value, actual, 0.00001);
  }

  @Test
  public void JSToJavaInt() throws Exception {
    double value = 4;
    double actual = (double) JSTypesUtil.jsToJava(new JSNumber(value));

    assertEquals(value, actual, 0.00001);
  }

  @Test
  public void JSToJavaNull() throws Exception {
    Object actual = JSTypesUtil.jsToJava(JSNull.get());

    assertEquals(null, actual);
  }

  @Test
  public void JSToJavaArray() throws Exception {
    JSArray array = (JSArray) new JSParser("['dd', 'ff', 'ddddd']").parse();
    List<String> actual = (List<String>) JSTypesUtil.jsToJava(array);
    List<String> expected = new ArrayList<>(Arrays.asList("dd", "ff", "ddddd"));
    assertEquals(expected, actual);
  }

  @Test
  public void JSToJavaArrayMess() throws Exception {
    JSArray array = (JSArray) new JSParser("[44, 'dd', 'ff', 'ddddd']").parse();
    List<Object> actual = (List<Object>) JSTypesUtil.jsToJava(array, true);
    List<Object> expected = new ArrayList<Object>(Arrays.asList(44, "dd", "ff", "ddddd"));
    assertEquals(expected, actual);
  }

  @Test
  public void javaToJSNumber() throws Exception {
    final int value = 4;
    JSValue expected = new JSNumber(value);
    JSValue actual = JSTypesUtil.javaToJS(value);

    assertEquals(expected, actual);
  }

  @Test
  public void javaToJSString() throws Exception {
    final String value = "dddf";
    JSValue expected = new JSString(value);
    JSValue actual = JSTypesUtil.javaToJS(value);

    assertEquals(expected, actual);
  }

  @Test
  public void javaToJSArray() throws Exception {
    final List<Object> values = Arrays.<Object>asList("dddf", 3);
    JSArray expected = new JSArray();
    expected.add(new JSString("dddf"));
    expected.add(new JSNumber(3));

    JSValue actual = JSTypesUtil.javaToJS(values);

    assertEquals(expected, actual);
  }
}