package retort.utils

import java.text.MessageFormat
import retort.utils.logging.Logger

class MessageUtil {


  private static boolean initialize = false

  private static Map messageSource

  public static boolean isInitialized() {
    return initialize
  }


  @NonCPS
  public static String getMessage(String code) {
    def message = messageSource[code]
    
    if (message) {
      return message
    }

    return code
  }

  @NonCPS
  public static String getMessage(String code, String... args) {
    
    def messageFormat = messageSource[code]
    
    if (messageFormat) {
      return MessageFormat.format(messageFormat, args)
    }

    return code
  }
  
  public static void setMessages(messages) {
    messageSource = messages
  }


}
