/*******************************************************************************
 * Copyright 2017 Albert Shun-Dat Chan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.github.javalbert.hibernate;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtils {
	public static SessionFactory createSessionFactory() {
		Properties properties = new Properties();
        properties.put("hibernate.connection.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        properties.put("hibernate.connection.username", "");
        properties.put("hibernate.connection.password", "");
        properties.put("hibernate.connection.pool_size", "1");
		properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
//		properties.put("hibernate.show_sql", "true");
		properties.put("hibernate.cache.use_query_cache", "false");
		
		Configuration configuration = new Configuration();
		configuration.setProperties(properties);
		
		configuration.addAnnotatedClass(DataTypeHolderHibernate.class);
		
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties())
				.build();
    	
		// CREDIT: http://stackoverflow.com/q/21645516
		// If cmd is closed while the benchmark is still running,
		// executing "mvn clean install" afterwards will not work because 
		// the target folder cannot be deleted.
		// Download EMCO UnLock IT to delete the target folder.
		// With this SessionFactoryObserver, when the benchmark finishes,
		// some system resource is released and "mvn clean install" can run
		configuration.setSessionFactoryObserver(
	        new SessionFactoryObserver() {
	            /**
				 * 
				 */
				private static final long serialVersionUID = -6313594801435114975L;
				@Override
	            public void sessionFactoryCreated(SessionFactory factory) {}
	            @Override
	            public void sessionFactoryClosed(SessionFactory factory) {
	            	StandardServiceRegistryBuilder.destroy(serviceRegistry);
	            }
	        }
		);
		
    	return configuration.buildSessionFactory(serviceRegistry);
	}
	
	private HibernateUtils() {}
}
