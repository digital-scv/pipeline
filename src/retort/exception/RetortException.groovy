package retort.exception

import retort.utils.MessageUtil

/**
 * Retort Exception
 */
class RetortException extends RuntimeException {

  private static final long serialVersionUID = 2764826915986236213L
  
  private String errorCode
  
  private String[] args
  

  /**
   * Constructs an <code>RetortException</code> with the
   * specified error code.
   *
   * @param   c   the errorcode.
   */
  public RetortException(String code) {
      super("[${code}] " + MessageUtil.getMessage(code))

      this.errorCode = code
  }
  
  public RetortException(String code, String... args) {
      super("[${code}] " + MessageUtil.getMessage(code, args))
      
      this.errorCode = code
      this.args = args
  }

  /**
   * Constructs a new exception with the specified error code and
   * cause.
   *
   * @param  the errorcode.
   * @param  cause the cause (which is saved for later retrieval by the
   *         {@link Throwable#getCause()} method).  (A <tt>null</tt> value
   *         is permitted, and indicates that the cause is nonexistent or
   *         unknown.)
   */
  public RetortException(String code, Throwable cause) {
      super("[${code}] " + MessageUtil.getMessage(code), cause);
      
      this.errorCode = code
  }
  
  public RetortException(String code, Throwable cause, String... args) {
      super("[${code}] " + MessageUtil.getMessage(code, args), cause);
      
      this.errorCode = code
      this.args = args
  }
  
  @NonCPS
  public String getErrorCode() {
    return this.errorCode
  }

  @NonCPS
  public String[] getArgs() {
    return this.args
  }


}