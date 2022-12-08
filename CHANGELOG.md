v0.2.0 (2022-12-19 17:40)
===========================

* __Security:__ Update Spring Boot to 2.7.6
* __Feature:__ Add computeIfAbsent method which reduces overhead when programatically creating or dropping
  caches, [click here for an example](https://github.com/KevinGuancheDarias/owge/blob/141f55e63ea588f875ef358b44de402f5727b068/business/src/main/java/com/kevinguanchedarias/owgejava/business/unit/HiddenUnitBo.java#L37)

v0.1.4 (2022-06-19 22:23)
==========================

* __Feature:__ Allow to use keySuffix to specify the key method arguments, this is useful when the methods have toString
  from lombok, and may be big
* __Fix:__ Don't throw when multiple threads attemp to write the same key... this may happen in overloaded environments,
  or strongly async projects
* __Feature:__ Allow to clear all the cache