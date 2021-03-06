/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.felix.http.jetty.httpservice;

import org.apache.felix.http.base.internal.DispatcherServlet;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.osgi.boot.JettyBootstrapActivator;
import org.eclipse.jetty.osgi.boot.OSGiWebappConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * Track org.eclipse.jetty.server.handler.ContextHandle. When a web app is deployed by jetty, this service is registered with some properties 
 * (see org.eclipse.jetty.osgi.boot.OSGiWebappConstants). -- Note that the real case is your first register this sevice with properties point to 
 * the web app folder path, then jetty deploy the web app. see http://wiki.eclipse.org/Jetty/Feature/Jetty_OSGi
 * 
 * This project achieve two purposes:
 *   (1) function as a web app bundle with the context path "/"
 *   (2) track the org.eclipse.jetty.server.handler.ContextHandle service. if the sevice's context path is "/", register the HttpService in the
 *       OSGI registry (to provide the HttpSerrvice service for other bundles to use).
 */
public class JettyContextHandlerServiceTracker implements ServiceListener
{
	//private Logger log = LoggerFactory.getLogger( JettyContextHandlerServiceTracker.class ) ;
	
    private DispatcherServlet dispatcher;
    private final BundleContext context;

    public JettyContextHandlerServiceTracker(BundleContext context, DispatcherServlet dispatcher)
    {
        this.context = context;
        this.dispatcher = dispatcher;
    }

    public void stop() throws Exception
    {
    }

    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param ev
     *            The <code>ServiceEvent</code> object.
     */
    public void serviceChanged(ServiceEvent ev)
    {
        ServiceReference sr = ev.getServiceReference();
        switch (ev.getType())
        {
            case ServiceEvent.MODIFIED:
            case ServiceEvent.UNREGISTERING:
            {
            	String contextPath = (String)sr.getProperty( OSGiWebappConstants.OSGI_WEB_CONTEXTPATH ) ;
            	if ( "/".equals( contextPath ) )
                	dispatcher.destroy() ;
            }
            if (ev.getType() == ServiceEvent.UNREGISTERING)
            {
                break;
            }
            else
            {
                // modified, meaning: we reload it. now that we stopped it;
                // we can register it.
            }
            case ServiceEvent.REGISTERED:
            {
            	/*
            	 * o.e.j.osig.boot.WebBundleTrackerCustomizer check if a bundle is a web bundle. 
            	 * If yes, if will invoke:
            	 *   JettyBootstrapActivator.registerWebapplication(bundle, warFolderRelativePath, contextPath);
            	 * JettyBootstrapActivator.registerWebapplication() simply instantiate a WebAppContext, then publish
            	 * it as the service of ContextHandler. JettyContextHandlerServiceTracker track the service, 
            	 * deploy the web bundle using WebBundleDeployHelper.
            	 */
            	String contextPath = (String)sr.getProperty( OSGiWebappConstants.OSGI_WEB_CONTEXTPATH ) ;
				System.out.println( "Context handler with Context path '" + contextPath + "' deployed" ) ;
            	if ( "/".equals( contextPath ) )
            	{
                    ContextHandler contextHandler = (ContextHandler)context.getService(sr);
                    ((WebAppContext)contextHandler).addServlet(new ServletHolder(dispatcher),"/*");
            	}

                break ;
            }
        }
    }
}
