/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.lang;

import java.io.Serializable;

import io.vertigo.core.locale.MessageKey;
import io.vertigo.core.locale.MessageText;

/**
 * Root Class for all user exceptions.
 * User Exceptions are built with a (localized) message
 *
 * A cause can be added by setting 'initCause' method
 *
 * @author fconstantin, pchretien
 */
public class VUserException extends RuntimeException {
	private static final long serialVersionUID = 3911465988816189879L;
	private final MessageText messageText;

	/**
	 * Constructor.
	 * @param messageText Message de l'exception
	 */
	public VUserException(final MessageText messageText) {
		//Attention il convient d'utiliser une méthode qui ne remonte d'exception.
		super(messageText.getDisplay());
		// On rerentre sur l'API des Exception en passant le message.
		this.messageText = messageText;
	}

	/**
	 * Constructor.
	 * @param defaultMsg the default msg (required)
	 * @param params  list of params (optional)
	 */
	public VUserException(final String defaultMsg, final Serializable... params) {
		this((MessageText.builder().withDefaultMsg(defaultMsg).withParams(params).build()));
	}

	/**
	 * Constructor.
	 * @param key  the msg key (required)
	 * @param params  list of params (optional)
	 */
	public VUserException(final MessageKey key, final Serializable... params) {
		this((MessageText.builder().withKey(key).withParams(params).build()));
	}

	/**
	 * Gestion des messages d'erreur externalisés.
	 * @return messageText.
	 */
	public final MessageText getMessageText() {
		return messageText;
	}
}
