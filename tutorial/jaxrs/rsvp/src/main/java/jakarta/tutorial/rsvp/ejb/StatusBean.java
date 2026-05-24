/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package jakarta.tutorial.rsvp.ejb;

import java.util.List;
import java.util.logging.Logger;

import jakarta.ejb.Stateless;
import jakarta.inject.Named;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.tutorial.rsvp.entity.Event;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ievans
 */
@Stateless
@Named
@Path("/status")
public class StatusBean {

    private List<Event> allCurrentEvents;
    private static final Logger logger = Logger.getLogger("jakarta.tutorial.rsvp.ejb.StatusBean");

    @PersistenceContext
    private EntityManager em;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{eventId}/")
    public Event getEvent(@PathParam("eventId") Long eventId)
    {
        final EntityGraph<?> entityGraph = em.getEntityGraph("graph.Events");
        final Map hints = new HashMap();
        hints.put("jakarta.persistence.fetchgraph", entityGraph);
        return em.find(Event.class, eventId, hints);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("all")
    public List<Event> getAllCurrentEvents() {
        logger.info("Calling getAllCurrentEvents");
        /* Problem solved using named entity graphs
         *
         * final EntityGraph<?> entityGraph = em.getEntityGraph("graph.Events");
         * final TypedQuery<Event> eventQuery =
         * em.createQuery("SELECT e FROM Event e", Event.class).setHint(
         * "jakarta.persistence.fetchgraph",
         * entityGraph);
         * allCurrentEvents = eventQuery.getResultList();
         */

 /* Problem solved using two left join fetch queries. It is necesary
         * to use two separate left join fetch queries to aviod the creation
         * of a cartesian product from te combination of the two joins
         */
        final TypedQuery<Event> eventResponseQuery =
            em.createNamedQuery("rsvp.entity.Event.getAllUpcomingEventsAndResponses",
                                Event.class);
        allCurrentEvents = eventResponseQuery.getResultList();
        final TypedQuery<Event> eventInviteesQuery =
            em.createNamedQuery("rsvp.entity.Event.getAllUpcomingEventsAndInvitees",
                                Event.class);
        allCurrentEvents = eventInviteesQuery.getResultList();
        if(allCurrentEvents == null) {
            logger.warning("No current events!");
        }
        return this.allCurrentEvents;
    }

    public void setAllCurrentEvents(List<Event> events) {
        allCurrentEvents = events;
    }
}
