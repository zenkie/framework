package nds.control.check;
import nds.control.event.NDSEventException;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.NDSException;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class ParseRegExpression {

  public Logger logger;
  public ParseRegExpression() {
      logger = LoggerManager.getInstance().getLogger(getClass().getName());
  }
  public void parseExpression(String regExpression,String input,String errorMessage) throws NDSException{
      /* The PatternCompiler interface defines the operations a regular expression compiler must implement.
       * However, the types of regular expressions recognized by a compiler and the Pattern implementations
       * produced as a result of compilation are not restricted.
       */
      PatternCompiler compiler = new Perl5Compiler();
      PatternMatcher matcher  = new Perl5Matcher();
      Pattern pattern = null;
      try {
           pattern = compiler.compile(regExpression);
      } catch(MalformedPatternException e) {
           logger.debug("正则表达式错误",e) ;
           throw new NDSEventException("正则表达式错误",e);

       }
      if(matcher.matches(input, pattern)){
          logger.debug(input+"is a value string") ;
      }else{
          logger.debug(input+"is not a value string") ;
          throw new NDSEventException(errorMessage);
      }

  }
}

/*
import org.apache.oro.text.perl.*;
import org.apache.oro.text.regex.*;
import org.apache.oro.text.GlobCompiler;


public final class Test {

  public static final void main(String args[]) {
      PatternCompiler compiler = new Perl5Compiler();
      PatternMatcher matcher  = new Perl5Matcher();
      Pattern pattern = null;
//    String regExpression = "^[a-z0-9A-Z_-]+@[a-z0-9A-Z_-]+\\.[a-z0-9A-Z_-]+";//"t.n";
//    String regExpression ="[1-9][0-4]?";
//    String regExpression ="[1-9][0-9][0-9][0-9]/[0-9]/[0-9]";
//    String regExpression ="1[0-9]|2[0-9]|3[0-1]|[1-9]|0[1-9]";
//    String regExpression ="1[0-2]|[1-9]|0[1-9]";
      String regExpression ="([1-9][0-9][0-9][0-9])/(1[0-2]|[1-9]|0[1-9])/(1[0-9]|2[0-9]|3[0-1]|[1-9]|0[1-9])";
      String input  = "2000/04/19";
 // Initialization of compiler, matcher, and input omitted;

       try {
           pattern = compiler.compile(regExpression);
       } catch(MalformedPatternException e) {
           System.out.println("Bad pattern.");
           System.out.println(e.getMessage());
           System.exit(1);
       }


       if(matcher.matches(input, pattern))
          System.out.println(input + " is a number");
       else
          System.out.println(input + " is not a number");

  }



}
*/