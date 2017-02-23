package com.metarhia.jstp.core;

import com.metarhia.jstp.core.JSTypes.JSArray;
import com.metarhia.jstp.core.JSTypes.JSBool;
import com.metarhia.jstp.core.JSTypes.JSNull;
import com.metarhia.jstp.core.JSTypes.JSNumber;
import com.metarhia.jstp.core.JSTypes.JSObject;
import com.metarhia.jstp.core.JSTypes.JSString;
import com.metarhia.jstp.core.JSTypes.JSUndefined;
import com.metarhia.jstp.core.JSTypes.JSValue;
import com.metarhia.jstp.core.Tokens.Token;
import com.metarhia.jstp.core.Tokens.Tokenizer;
import java.io.Serializable;

public class JSParser implements Serializable {

  public static final boolean VERBOSE_CHECKING = true;
  private Tokenizer tokenizer;

  public JSParser() {
    tokenizer = new Tokenizer("");
  }

  public JSParser(String input) {
    tokenizer = new Tokenizer(input);
  }

  public static JSValue parse(String input) throws JSParsingException {
    return new JSParser(input).parse();
  }

  public JSValue parse() throws JSParsingException {
    return parse(true);
  }

  public JSValue parse(boolean skip) throws JSParsingException {
    if (skip) {
      tokenizer.next();
    }

    switch (tokenizer.getLastToken()) {
      case TRUE:
        return new JSBool(true);
      case FALSE:
        return new JSBool(false);
      case STRING:
        return new JSString(tokenizer.getStr());
      case CURLY_OPEN:
        return parseObject();
      case SQ_OPEN:
        return parseArray();
      case NUMBER:
        return new JSNumber(tokenizer.getNumber());
      case NULL:
        return JSNull.get();
      default:
        return JSUndefined.get();
    }
  }

  public JSArray parseArray() throws JSParsingException {
    assureToken("Error: expected '[' at the beginning of JSArray", Token.SQ_OPEN);

    JSArray jsArray = new JSArray();
    while (tokenizer.getLastToken() != Token.SQ_CLOSE
        && tokenizer.next() != Token.SQ_CLOSE) {
      if (tokenizer.getLastToken() == Token.COMMA) {
        jsArray.add(JSUndefined.get());
        continue;
      } else {
        jsArray.add(parse(false));
      }
      // skip comma
      if (tokenizer.next() != Token.COMMA && tokenizer.getLastToken() != Token.SQ_CLOSE) {
        throw new JSParsingException(tokenizer.getPrevIndex(),
            "Expected ',' as separator of array elements");
      }
    }
    return jsArray;
  }

  public JSObject parseObject() throws JSParsingException {
    assureToken("Expected '{' at the beginning of JSObject", Token.CURLY_OPEN);

    JSObject jsObject = new JSObject();
    while (tokenizer.getLastToken() != Token.CURLY_CLOSE
        && tokenizer.next() != Token.CURLY_CLOSE) {
      jsObject.put(parseKeyValuePair());
      // skip comma
      if (tokenizer.next() != Token.COMMA && tokenizer.getLastToken() != Token.CURLY_CLOSE) {
        throw new JSParsingException(tokenizer.getPrevIndex(),
            "Expected ',' as key-value pairs separator");
      }
    }
    return jsObject;
  }

  public JSObject.Entry parseKeyValuePair() throws JSParsingException {
    assureToken("Expected valid key", Token.KEY, Token.NUMBER, Token.STRING);

    String key = tokenizer.getStr();

    if (tokenizer.next() != Token.COLON) {
      throw new JSParsingException(tokenizer.getPrevIndex(),
          "Expected ':' as separator of Key and Value");
    }

    JSValue value = parse(true);

    return new JSObject.Entry(key, value);
  }

  private void assureToken(String errorMsg, Token... tokens) throws JSParsingException {
    if (!VERBOSE_CHECKING) {
      return;
    }

    boolean assured = assure(tokenizer.getLastToken(), tokens);
    if (!assured) {
      assured = assure(tokenizer.next(), tokens);
      if (!assured) {
        throw new JSParsingException(tokenizer.getPrevIndex(), errorMsg);
      }
    }
  }

  private boolean assure(Token tokenToAssure, Token[] array) {
    for (Token token : array) {
      if (token == tokenToAssure) {
        return true;
      }
    }
    return false;
  }

  public void setInput(String input) {
    tokenizer = new Tokenizer(input);
  }
}
