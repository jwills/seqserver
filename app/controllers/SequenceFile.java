package controllers;

import play.*;
import play.libs.F.Option;
import play.mvc.*;

public class SequenceFile extends Controller {
  public static Result index(String path, Option<Long> offset, Option<String> key) {
    return ok(String.format("P: %s and o: %d and k: %s", path, offset.getOrElse(17L), key.getOrElse("josh")));
  }
}
