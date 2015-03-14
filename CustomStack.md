# How to use other stack with Odiem. #

If you want to use other ldap stack, you have to implement the following interfaces:

  1. **org.odiem.api.OdmDriver**
  1. **org.odiem.api.OdmStackFactory**
  1. **org.odiem.api.OdmStack**

Add to META-INF/services folder of your jar a text file named **`org.odiem.api.OdmDriver`**.
Write in this file the full packaged name of your OdmDriver implementations

```
package.to.my.OdmDriverImpl
package.to.mySecond.OdmDriverImpl
```

This file allow odiem to autodetect odiem driver implementatios (analogue pattern adopted for jdbc drivers).

Now you can use your custom stack passing it your custom properties:
```
OdmConnectionFactory odmFactory = OdmDriverManager.getConnectionFactory("myDriverName", properties);
```

See JNDI stack [source code](http://code.google.com/p/odiem/source/browse/#svn/trunk/src/org/odiem/stacks/jndi) for references..