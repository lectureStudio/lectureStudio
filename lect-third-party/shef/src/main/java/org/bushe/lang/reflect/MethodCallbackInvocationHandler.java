/**
 * Copyright 2005 Bushe Enterprises, Inc., Hopkinton, MA, USA, www.bushe.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bushe.lang.reflect;

import java.lang.reflect.*;

/**
 * An implementation of the {@link InvocationHandler} interface that
 * can be used to create a dynamic proxy that, when invoked, will
 * call a specified method.  This allows any class to essentially
 * implement an interface without declaring that is implements the
 * interface, and have any method called when the methods of that
 * interface are invoked.
 * <p>
 * The method can be called on a provided instance or can be a static
 * class method.
 * <p>
 * The method can accept the args of the interface method, or accept
 * no args at all.
 * <p>
 * See {@link org.bushe.swing.action.ActionManager} for an example
 * usage that allows any class to receive callbacks for ActionListener
 * without implementing ActionListener.
 * <p>
 * @author Michael Bushe
 * @version 1.0
 */
public class MethodCallbackInvocationHandler implements InvocationHandler {

    Object target;
    Method method;
    boolean useArgs;

    public static Object createMethodCallbackProxy(Object callback, String method, Class[] args,
                                             Class implementingInterface, Class[] arrayOfInterfaces)
        throws UnsupportedOperationException, SecurityException, NoSuchMethodException,
        IllegalArgumentException {
        if (callback == null) {
            throw new IllegalArgumentException("Handler cannot be null.");
        }
        if (method == null) {
            throw new IllegalArgumentException("Method cannot be null.");
        }
        //get the callback class and the method to invoke
        boolean methodShouldBeStatic = false;
        Method methodToInvoke = null;
        Class callbackClass = null;
        if (callback instanceof Class) {
            methodShouldBeStatic = true;
            callbackClass = (Class) callback;
        } else {
            callbackClass = callback.getClass();
        }

        //first look for action event signature
        try {
            methodToInvoke = callbackClass.getMethod(method, args);
        } catch (NoSuchMethodException ex) {
            //then use the no arg signature
            methodToInvoke = callbackClass.getMethod(method, (Class[])null);
        }
        if (methodToInvoke == null) {
            throw new NoSuchMethodException("Callback Method named " + method
                            + " not found in class " + callbackClass);

        }
        boolean isStaticMethod = Modifier.isStatic(methodToInvoke.getModifiers());
        if (isStaticMethod && methodShouldBeStatic) {
            throw new NoSuchMethodException("Callback Method named " + method
                                            + " must be static in class " + callbackClass);
        }
        return Proxy.newProxyInstance(MethodCallbackInvocationHandler.class.getClassLoader(),
            arrayOfInterfaces,
            new MethodCallbackInvocationHandler(implementingInterface, callback, methodToInvoke));
    }

    /**
     * Create a handler implements the given interface by calling the
     * given method on the given object, passing the args of the inteface
     * calls if specified.
     *
     * @param interfac the interface this proxy handler implements
     * @param callback the object that will be called, ignored (can be null) for
     * static methods.
     * @param m the method to invoke on the target (or on the class if the
     * method is static), cannot be null
     * @throws IllegalArgumentException if method is null, or if target is
     * null and the method is not static, or if the args of the method
     * are anything except one ActionEvent or none at all.
     */
    public MethodCallbackInvocationHandler(Class interfac, Object callback, Method m) {
        this.target = callback;
        this.method = m;
        if (m == null) {
            throw new IllegalArgumentException("Method is null.");
        }
        if (!Modifier.isStatic(m.getModifiers()) && target == null) {
            throw new IllegalArgumentException("Method is static and target is null.");
        }
        Class[] cl = m.getParameterTypes();
        if (cl.length > 0) {
            this.useArgs = true;
        }
    }

    /**
     * InvocationHandler implementation, invokes the method
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (useArgs) {
            this.method.invoke(target, args);
        } else {
            this.method.invoke(target, (Object[])null);
        }
        return null;
    }
}