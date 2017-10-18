package com.snapswap.telesign.model.external

object AccountLifecycleEventEnum extends Enumeration {
  type TelesignAccountLifecycleEvent = Value

  val create, sign, transact, update, delete = Value
}
