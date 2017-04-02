package com.metarhia.jstp.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.metarhia.jstp.core.JSTypes.JSUndefined;
import com.metarhia.jstp.core.TestUtils.TestData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Created by lundibundi on 4/1/17.
 */
class JSNativeParserTest {

  public static final TestData[] parseTestData = new TestData[]{
      new TestData<>("[,,0]", Arrays.asList(
          JSUndefined.get(), JSUndefined.get(), 0.0)),
      new TestData<>("", JSUndefined.get()),
      new TestData<>("{nickname: '\\n\\tnyaaaaaa\\'aaa\\'[((:’ –( :-)) :-| :~ =:O)],'}",
          TestUtils.mapOf(
              "nickname", "\n\tnyaaaaaa\'aaa\'[((:’ –( :-)) :-| :~ =:O)],")),
      new TestData<>("{nickname:\"\\n\\tnyaaaaaa'aaa'[((:’ –( :-)) :-| :~ =:O)],\"}",
          TestUtils.mapOf(
              "nickname", "\n\tnyaaaaaa\'aaa\'[((:’ –( :-)) :-| :~ =:O)],")),
      new TestData<>("[ 'abs', 'smth else', \" or like this \", ['inside', 'elsein']]",
          Arrays.asList("abs", "smth else", " or like this ",
              Arrays.asList("inside", "elsein"))),
      new TestData<>("{a: 1, b: 2.0, c: '5555'}", TestUtils.mapOf(
          "a", 1.0,
          "b", 2.0,
          "c", "5555")
      ),
      new TestData<>("[1,,300]", Arrays.asList(1.0, JSUndefined.get(), 300.0)),

      new TestData<>("[1,2,'5555']", Arrays.asList(1.0, 2.0, "5555")),
      new TestData<>("true", true),
      new TestData<>("false", false),
      new TestData<>("10", 10.0),
      new TestData<>("63.52", 63.52),
      new TestData<>("undefined", JSUndefined.get()),
      new TestData<>("null", null),
      new TestData<>("{birth:-2051225940000}", TestUtils.mapOf(
          "birth", -2051225940000.0)),
  };

  private static final TestData[] parseKeyValuePairTestData = new TestData[]{
      new TestData<>("a: 4", new JSNativeParser.KeyValuePair<>("a", 4.0)),
      new TestData<>("55 : ['abc']", new JSNativeParser.KeyValuePair<>(
          "55.0", Arrays.asList("abc")))
  };

  private static final TestData[] parseThrowTestData = new TestData[]{
      new TestData<>("{he : llo : 123}", new JSParsingException(
          "Index: 9, Message: Expected ',' as key-value pairs separator")),
      new TestData<>("{he : llo : 123}", new JSParsingException(
          "Index: 9, Message: Expected ',' as key-value pairs separator")),
      new TestData<>("{'ssssss : }", new JSParsingException(
          "Index: 1, Message: Unmatched quote")),
      new TestData<>("'ssssss", new JSParsingException(
          "Index: 0, Message: Unmatched quote"))
  };

  private JSNativeParser parser;

  public JSNativeParserTest() {
    parser = new JSNativeParser();
  }

  @Test
  public void parseTest() throws Exception {
    for (TestData<String, Object> td : parseTestData) {
      parser.setInput(td.input);
      Object actual = parser.parse();
      assertEquals(td.expected, actual);
    }
  }

  @Test
  public void parseKeyValuePair() throws Exception {
    for (TestData<String, JSNativeParser.KeyValuePair> td : parseKeyValuePairTestData) {
      parser.setInput(td.input);
      JSNativeParser.KeyValuePair actual = parser.parseKeyValuePair();
      assertEquals(td.expected.getKey(), actual.getKey());
      assertEquals(td.expected.getValue(), actual.getValue());
    }
  }

  @Test
  public void parseThrow() throws Exception {
    for (TestData<String, JSParsingException> td : parseThrowTestData) {
      Exception exception = null;
      try {
        parser.setInput(td.input);
        parser.parse();
      } catch (JSParsingException e) {
        exception = e;
      }
      assertNotNull(exception);
      assertEquals(td.expected.getMessage(), exception.getMessage());
    }
  }

  @Test
  public void testPacketSample() throws Exception {
    String input = "{\n" +
        "  name: 'Marcus Aurelius',\n" +
        "  passport: 'AE127095',\n" +
        "  birth: {\n" +
        "    date: '1990-02-15',\n" +
        "    place: 'Rome'\n" +
        "  },\n" +
        "  contacts: {\n" +
        "    email: 'marcus@aurelius.it',\n" +
        "    phone: '+380505551234',\n" +
        "    address: {\n" +
        "      country: 'Ukraine',\n" +
        "      city: 'Kiev',\n" +
        "      zip: '03056',\n" +
        "      street: 'Pobedy',\n" +
        "      building: '37',\n" +
        "      floor: '1',\n" +
        "      room: ['158', '111', '555']\n" +
        "    }\n" +
        "  }\n" +
        "}";

    Map<String, Object> expected = new LinkedHashMap<>();
    expected.put("name", "Marcus Aurelius");
    expected.put("passport", "AE127095");

    Map<String, Object> nestedBirth = new LinkedHashMap<>();
    nestedBirth.put("date", "1990-02-15");
    nestedBirth.put("place", "Rome");
    expected.put("birth", nestedBirth);
    Map<String, Object> nestedContacts = new LinkedHashMap<>();
    nestedContacts.put("email", "marcus@aurelius.it");
    nestedContacts.put("phone", "+380505551234");
    Map<String, Object> nnAddress = new LinkedHashMap<>();
    nnAddress.put("country", "Ukraine");
    nnAddress.put("city", "Kiev");
    nnAddress.put("zip", "03056");
    nnAddress.put("street", "Pobedy");
    nnAddress.put("building", "37");
    nnAddress.put("floor", "1");
    List<Object> roomArray = new ArrayList<>();
    roomArray.add("158");
    roomArray.add("111");
    roomArray.add("555");
    nnAddress.put("room", roomArray);
    nestedContacts.put("address", nnAddress);
    expected.put("contacts", nestedContacts);

    parser.setInput(input);
    Map<String, Object> actual = parser.parseObject();

    assertEquals(expected, actual);
  }
}