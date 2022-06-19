v0.1.4 (2022-06-19 __22:__23)
==========================

* __Feature:__ Allow to use keySuffix to specify the key method arguments, this is useful when the methods have toString from lombok, and may be big
* __Fix:__ Don't throw when multiple threads attemp to write the same key... this may happen in overloaded environments, or strongly async projects
* __Feature:__ Allow to clear all the cache