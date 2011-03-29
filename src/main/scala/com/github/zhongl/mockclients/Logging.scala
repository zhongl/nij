package com.github.zhongl.mockclients


import org.slf4j.LoggerFactory

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
trait Logging {
  @transient val log = LoggerFactory.getLogger(this.getClass)
}
