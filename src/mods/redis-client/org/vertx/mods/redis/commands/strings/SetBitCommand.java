/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vertx.mods.redis.commands.strings;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.mods.redis.CommandContext;
import org.vertx.mods.redis.commands.Command;
import org.vertx.mods.redis.commands.CommandException;

import redis.clients.jedis.exceptions.JedisException;

/**
 * SetBitCommand
 * <p>
 * 
 * @author <a href="http://marx-labs.de">Thorsten Marx</a>
 */
public class SetBitCommand extends Command {
	
	public static final String COMMAND = "setbit";

	public SetBitCommand () {
		super(COMMAND);
	}
	
	@Override
	public void handle(Message<JsonObject> message, CommandContext context) throws CommandException {
		String key = getMandatoryString("key", message);
		if (key == null) {
			sendError(message, "key can not be null");
			return;
		}
		Number offset = message.body.getNumber("offset");
		checkNull(offset, "offset can not be null");
		checkType(offset, Integer.class, "offset must be an integer or long");
		checkType(offset, Long.class, "offset must be an integer or long");
		
		Boolean value = message.body.getBoolean("value");
		checkNull(value, "value can not be null");
		
		try {
			value = context.getClient().setbit(key, offset.longValue(), value);
			
			JsonObject reply = new JsonObject().putBoolean("value", value);
			sendOK(message, reply);
		} catch (JedisException e) {
			sendError(message, e.getLocalizedMessage());
		}

	}
}
