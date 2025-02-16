/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.metamodel.facets.actions.action;

import java.lang.reflect.Method;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract;
import org.apache.isis.core.metamodel.facets.members.disabled.method.DisableForContextFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethod;
import org.apache.isis.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethod;
import org.apache.isis.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.testspec.ObjectSpecificationStub;

public class ActionAnnotationFacetFactoryTest_actionInvocation extends AbstractFacetFactoryTest {

    private final ObjectSpecification voidSpec = new ObjectSpecificationStub("VOID");
    private final ObjectSpecification stringSpec = new ObjectSpecificationStub("java.lang.String");
    private final ObjectSpecification customerSpec = new ObjectSpecificationStub("Customer");
    private ActionAnnotationFacetFactory facetFactory;

    public void setUp() throws Exception {
        super.setUp();
        facetFactory = new ActionAnnotationFacetFactory();
        facetFactory.setSpecificationLoader(programmableReflector);
    }

    public void testActionInvocationFacetIsInstalledAndMethodRemoved() {
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processInvocation(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionInvocationFacetForDomainEventAbstract);
        final ActionInvocationFacetForDomainEventAbstract actionInvocationFacetViaMethod = (ActionInvocationFacetForDomainEventAbstract) facet;
        assertEquals(actionMethod, actionInvocationFacetViaMethod.getMethods().get(0));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(actionMethod));
    }

    public void testActionReturnTypeWhenVoid() {
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processInvocation(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
        final ActionInvocationFacetForDomainEventAbstract actionInvocationFacetViaMethod = (ActionInvocationFacetForDomainEventAbstract) facet;
        assertEquals(voidSpec, actionInvocationFacetViaMethod.getReturnType());
    }

    public void testActionReturnTypeWhenNotVoid() {
        programmableReflector.setLoadSpecificationStringReturn(stringSpec);

        class Customer {
            @SuppressWarnings("unused")
            public String someAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processInvocation(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
        final ActionInvocationFacetForDomainEventAbstract actionInvocationFacetViaMethod = (ActionInvocationFacetForDomainEventAbstract) facet;
        assertEquals(stringSpec, actionInvocationFacetViaMethod.getReturnType());
    }

    public void testActionOnType() {
        programmableReflector.setLoadSpecificationStringReturn(customerSpec);

        class Customer {
            @SuppressWarnings("unused")
            public String someAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processInvocation(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
        final ActionInvocationFacetForDomainEventAbstract actionInvocationFacetViaMethod = (ActionInvocationFacetForDomainEventAbstract) facet;
        assertEquals(customerSpec, actionInvocationFacetViaMethod.getOnType());
    }

    public void testActionsPickedUpFromSuperclass() {
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {
            }
        }

        class CustomerEx extends Customer {
        }

        final Method actionMethod = findMethod(CustomerEx.class, "someAction", new Class[] { int.class, long.class });

        final FacetedMethod facetHolderWithParms = FacetedMethod.createForAction(CustomerEx.class, actionMethod);

        facetFactory.processInvocation(new ProcessMethodContext(CustomerEx.class, null, null, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getFacet(ActionInvocationFacet.class);
        assertNotNull(facet0);
    }

    public void testActionsPickedUpFromSuperclassButHelpersFromSubClass() {
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        final ActionParameterChoicesFacetViaMethodFactory facetFactoryForChoices = new ActionParameterChoicesFacetViaMethodFactory();
        facetFactoryForChoices.setSpecificationLoader(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        final DisableForContextFacetViaMethodFactory facetFactoryForDisable = new DisableForContextFacetViaMethodFactory();
        facetFactoryForDisable.setSpecificationLoader(programmableReflector);
        programmableReflector.setLoadSpecificationStringReturn(voidSpec);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {
            }

            @SuppressWarnings("unused")
            public int[] choices0SomeAction() {
                return new int[0];
            }
        }

        class CustomerEx extends Customer {
            @Override
            public int[] choices0SomeAction() {
                return new int[0];
            }

            @SuppressWarnings("unused")
            public long[] choices1SomeAction() {
                return new long[0];
            }

            @SuppressWarnings("unused")
            public String disableSomeAction(final int x, final long y) {
                return null;
            }
        }

        final Method actionMethod = findMethod(CustomerEx.class, "someAction", new Class[] { int.class, long.class });
        final Method choices0Method = findMethod(CustomerEx.class, "choices0SomeAction", new Class[] {});
        final Method choices1Method = findMethod(CustomerEx.class, "choices1SomeAction", new Class[] {});
        final Method disableMethod = findMethod(CustomerEx.class, "disableSomeAction", new Class[] { int.class, long.class });

        final FacetedMethod facetHolderWithParms = FacetedMethod.createForAction(CustomerEx.class, actionMethod);

        final ProcessMethodContext processMethodContext = new ProcessMethodContext(CustomerEx.class, null, null, actionMethod, methodRemover, facetHolderWithParms);
        facetFactory.processInvocation(processMethodContext);

        facetFactoryForChoices.process(processMethodContext);
        facetFactoryForDisable.process(processMethodContext);

        final Facet facet0 = facetHolderWithParms.getFacet(ActionInvocationFacet.class);
        assertNotNull(facet0);

        final Facet facet1 = facetHolderWithParms.getParameters().get(0).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet1);
        assertTrue(facet1 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod0 = (ActionParameterChoicesFacetViaMethod) facet1;
        assertEquals(choices0Method, actionChoicesFacetViaMethod0.getMethods().get(0));

        final Facet facet2 = facetHolderWithParms.getParameters().get(1).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet2);
        assertTrue(facet2 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod1 = (ActionParameterChoicesFacetViaMethod) facet2;
        assertEquals(choices1Method, actionChoicesFacetViaMethod1.getMethods().get(0));

        final Facet facet3 = facetHolderWithParms.getFacet(DisableForContextFacet.class);
        assertNotNull(facet3);
        assertTrue(facet3 instanceof DisableForContextFacetViaMethod);
        final DisableForContextFacetViaMethod disableFacetViaMethod = (DisableForContextFacetViaMethod) facet3;
        assertEquals(disableMethod, disableFacetViaMethod.getMethods().get(0));
    }
}
