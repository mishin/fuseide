/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.fusesource.ide.jmx.commons.messages;

import java.util.Map;

/**
 * Represents a message exchange in ActiveMQ, Camel, CXF or ServiceMix
 *
 */
public interface IExchange extends Comparable<IExchange> {
	public String getId();
	
	public Map<String,Object> getProperties();

	public IMessage getIn();
	public IMessage getOut();

}
