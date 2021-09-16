/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.backend.server.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.services.cdi.Startable;
import org.uberfire.commons.services.cdi.Startup;
import org.uberfire.commons.services.cdi.StartupType;
import org.uberfire.commons.services.cdi.Veto;
import org.uberfire.spaces.Space;
import org.uberfire.spaces.SpacesAPI;

public class SystemConfigProducer implements Extension {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigProducer.class);

    private static final String CDI_METHOD = "cdi";

    private static final String START_METHOD = System.getProperty("org.uberfire.start.method",
                                                                  "cdi");
    protected static final String SYSTEM = "system";

    private final List<OrderedBean> startupEagerBeans = new LinkedList<>();
    private final List<OrderedBean> startupBootstrapBeans = new LinkedList<>();
    private final Comparator<OrderedBean> priorityComparator = (o1, o2) -> o1.priority - o2.priority;
    private boolean systemFSNotExists = true;
    private boolean ioStrategyBeanNotFound = true;


    public <X> void processBean(@Observes final ProcessBean<X> event) {
        if (event.getBean().getName() != null && event.getBean().getName().equals("systemFS")) {
            systemFSNotExists = false;
        } else if (event.getBean().getName() != null && event.getBean().getName().equals("ioStrategy")) {
            ioStrategyBeanNotFound = false;
        }
        if (event.getAnnotated().isAnnotationPresent(Startup.class) && (event.getAnnotated().isAnnotationPresent(ApplicationScoped.class)
                || event.getAnnotated().isAnnotationPresent(Singleton.class))) {
            final Startup startupAnnotation = event.getAnnotated().getAnnotation(Startup.class);
            final StartupType type = startupAnnotation.value();
            final int priority = startupAnnotation.priority();
            final Bean<?> bean = event.getBean();
            switch (type) {
                case EAGER:
                    startupEagerBeans.add(new OrderedBean(bean,
                                                          priority));
                    break;
                case BOOTSTRAP:
                    startupBootstrapBeans.add(new OrderedBean(bean,
                                                              priority));
                    break;
            }
        } else if (event.getAnnotated().isAnnotationPresent(Named.class) && (event.getAnnotated().isAnnotationPresent(ApplicationScoped.class)
                || event.getAnnotated().isAnnotationPresent(Singleton.class))) {
            final Named namedAnnotation = event.getAnnotated().getAnnotation(Named.class);

            if (namedAnnotation.value().endsWith("-startable")) {
                final Bean<?> bean = event.getBean();
                startupBootstrapBeans.add(new OrderedBean(bean,
                                                          10));
            }
        }
    }

    public void afterDeploymentValidation(final @Observes AfterDeploymentValidation event,
                                          final BeanManager manager) {
        if (CDI_METHOD.equalsIgnoreCase(START_METHOD)) {
            //Force execution of Bootstrap bean's @PostConstruct methods first
            runPostConstruct(manager,
                             startupBootstrapBeans);

            //Followed by execution of remaining Eager bean's @PostConstruct methods
            runPostConstruct(manager,
                             startupEagerBeans);
        }
    }

    private void runPostConstruct(final BeanManager manager,
                                  final List<OrderedBean> orderedBeans) {
        //Sort first, by priority
        Collections.sort(orderedBeans,
                         priorityComparator);
        for (OrderedBean ob : orderedBeans) {
            // the call to toString() is a cheat to force the bean to be initialized
            final Bean<?> bean = ob.bean;
            manager.getReference(bean,
                                 bean.getBeanClass(),
                                 manager.createCreationalContext(bean)).toString();
        }
    }

    <T> void processAnnotatedType(@Observes @WithAnnotations(Veto.class) ProcessAnnotatedType<T> pat) {
        pat.veto();
    }

    void afterBeanDiscovery(@Observes final AfterBeanDiscovery abd,
                            final BeanManager bm) {
        if (!CDI_METHOD.equalsIgnoreCase(START_METHOD)) {
            buildStartableBean(abd, bm);
        }
    }


    URI resolveFSURI(SpacesAPI spaces, Space space, String fsName) {

        return spaces.resolveFileSystemURI(SpacesAPI.Scheme.DEFAULT,
                                           space,
                                           fsName);
    }

    SpacesAPI getSpaces(BeanManager bm) {
        final Bean<SpacesAPI> spacesBean = (Bean<SpacesAPI>) bm.getBeans(SpacesAPI.class).iterator().next();
        final CreationalContext<SpacesAPI> spacesCtx = bm.createCreationalContext(spacesBean);
        return (SpacesAPI) bm.getReference(spacesBean,
                                           SpacesAPI.class,
                                           spacesCtx);
    }


    private <T> T getBean(BeanManager bm,
                          Class<T> clazz) {
        final Bean<T> bean = (Bean<T>) bm.getBeans(clazz).iterator().next();
        return getBeanReference(bm,
                                clazz,
                                bean);
    }

    private <T> T getBean(BeanManager bm,
                          Class<T> clazz,
                          Annotation qualifier) {
        final Bean<T> bean = (Bean<T>) bm.getBeans(clazz,
                                                   qualifier).iterator().next();
        return getBeanReference(bm,
                                clazz,
                                bean);
    }

    private <T> T getBeanReference(BeanManager bm,
                                   Class<T> clazz,
                                   Bean<T> bean) {
        final CreationalContext<T> creationalContext = bm.createCreationalContext(bean);
        return (T) bm.getReference(bean,
                                   clazz,
                                   creationalContext);
    }

    private void buildStartableBean(final AfterBeanDiscovery abd,
                                    final BeanManager bm) {

        abd.addBean(new Bean<Startable>() {

            @Override
            public Class<?> getBeanClass() {
                return Startable.class;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return Collections.emptySet();
            }

            @Override
            public String getName() {
                return "startablebean";
            }

            @Override
            public Set<Annotation> getQualifiers() {

                return new HashSet<Annotation>() {{
                    add(new AnnotationLiteral<Default>() {
                    });
                    add(new AnnotationLiteral<Any>() {
                    });
                }};
            }

            @Override
            public Class<? extends Annotation> getScope() {
                return ApplicationScoped.class;
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            @Override
            public Set<Type> getTypes() {
                return new HashSet<Type>() {{
                    add(Startable.class);
                    add(Object.class);
                }};
            }

            @Override
            public boolean isAlternative() {
                return false;
            }

            @Override
            public boolean isNullable() {
                return false;
            }

            @Override
            public Startable create(CreationalContext<Startable> ctx) {

                return new Startable() {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }

                    @Override
                    public void start() {
                        //Force execution of Bootstrap bean's @PostConstruct methods first
                        runPostConstruct(bm,
                                         startupBootstrapBeans);

                        //Followed by execution of remaining Eager bean's @PostConstruct methods
                        runPostConstruct(bm,
                                         startupEagerBeans);
                    }
                };
            }

            @Override
            public void destroy(final Startable instance,
                                final CreationalContext<Startable> ctx) {

                ctx.release();
            }
        });
    }

    private class OrderedBean {

        Bean<?> bean;
        int priority;

        private OrderedBean(final Bean<?> bean,
                            final int priority) {
            this.bean = bean;
            this.priority = priority;
        }
    }

    public class NamedLiteral extends AnnotationLiteral<Named> implements Named {

        private final String value;

        public NamedLiteral(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }
}
