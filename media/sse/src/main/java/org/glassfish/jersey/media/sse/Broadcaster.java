/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.media.sse;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Used for broadcasting sse to multiple {@link EventChannel} instances.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class Broadcaster {

    private final ConcurrentSkipListSet<EventChannel> eventChannelSet = new ConcurrentSkipListSet<EventChannel> (new Comparator<EventChannel>() {
        @Override
        public int compare(EventChannel eventChannel, EventChannel eventChannel1) {
            return eventChannel.hashCode() - eventChannel1.hashCode();
        }
    });

    /**
     * Register {@link EventChannel} to current {@link Broadcaster} instance.
     *
     * @param eventChannel {@link EventChannel} to register.
     */
    public void registerEventChannel(EventChannel eventChannel) {
        eventChannelSet.add(eventChannel);
    }

    /**
     * Broadcast an {@link OutboundEvent} to all registered {@link EventChannel} instances.
     *
     * @param outboundEvent event to be sent.
     * @throws java.io.IOException when
     */
    public void broadcast(OutboundEvent outboundEvent) throws IOException {
        for (Iterator<EventChannel> iterator = eventChannelSet.iterator(); iterator.hasNext();) {
            EventChannel eventChannel = iterator.next();
            if(eventChannel.isClosed()) {
                iterator.remove();
            } else {
                eventChannel.write(outboundEvent);
            }
        }
    }

    /**
     * Close all registered {@link EventChannel} instances.
     */
    public void close() throws IOException {
        for(EventChannel eventChannel : eventChannelSet) {
            eventChannel.close();
        }
    }
}
