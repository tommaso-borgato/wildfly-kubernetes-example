/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * A simple servlet taking advantage of features added in 3.0.
 * </p>
 *
 * <p>
 * The servlet is registered and mapped to /HelloServlet using the {@linkplain WebServlet
 *
 * @author Pete Muir
 * @HttpServlet}. The {@link HostNameService } is injected by CDI.
 * </p>
 */
@WebServlet("/")
public class HostNameServlet extends HttpServlet {

	protected static final Logger log = Logger.getLogger(HostNameServlet.class.getName());

	public static final String KEY = HostNameServlet.class.getName();

	@Inject
	HostNameService hostNameService;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session = req.getSession(true);

		Integer serial = 1;
		if (!session.isNew()) {
			serial = (Integer) session.getAttribute(KEY);
			serial++;
		} else {
			log.log(Level.INFO, "New session created: {0} with {1} serial", new Object[]{session.getId(), serial});
		}
		session.setAttribute(KEY, serial);

		resp.setContentType("text/html");
		PrintWriter writer = resp.getWriter();
		writer.println("{ \"hostname\"=\"" + hostNameService.createHelloMessage() + "\", \"serial\"=\"" + serial + "\"}");
		writer.close();
	}

}
