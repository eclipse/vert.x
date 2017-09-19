/*
 * Copyright (c) 2011-2017 The original author or authors
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 and the Apache License, Version 2.0
 * which accompanies this distribution. The Eclipse Public License 2.0 is
 * available at http://www.eclipse.org/legal/epl-2.0.html, and the Apache
 * License, Version 2.0 is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

/**
 * == JSON
 * :toc: left
 *
 * Unlike some other languages, Java does not have first class support for http://json.org/[JSON] so we provide
 * two classes to make handling JSON in your Vert.x applications a bit easier.
 *
 * === JSON objects
 *
 * The {@link io.vertx.core.json.JsonObject} class represents JSON objects.
 *
 * A JSON object is basically just a map which has string keys and values can be of one of the JSON supported types
 * (string, number, boolean).
 *
 * JSON objects also support null values.
 *
 * ==== Creating JSON objects
 *
 * Empty JSON objects can be created with the default constructor.
 *
 * You can create a JSON object from a string JSON representation as follows:
 *
 * [source,java]
 * ----
 * {@link docoverride.json.Examples#example0_1}
 * ----
 *
 * You can create a JSON object from a map as follows:
 *
 * [source,java]
 * ----
 * {@link docoverride.json.Examples#exampleCreateFromMap}
 * ----
 *
 * ==== Putting entries into a JSON object
 *
 * Use the {@link io.vertx.core.json.JsonObject#put} methods to put values into the JSON object.
 *
 * The method invocations can be chained because of the fluent API:
 *
 * [source,java]
 * ----
 * {@link docoverride.json.Examples#example1}
 * ----
 *
 * ==== Getting values from a JSON object
 *
 * You get values from a JSON object using the {@code getXXX} methods, for example:
 *
 * [source,java]
 * ----
 * {@link docoverride.json.Examples#example2}
 * ----
 *
 * ==== Mapping between JSON objects and Java objects
 *
 * You can create a JSON object from the fields of a Java object as follows:
 *
 * You can instantiate a Java object and populate its fields from a JSON object as follows:
 *
 * [source,java]
 * ----
 * {@link docoverride.json.Examples#mapToPojo}
 * ----
 *
 * Note that both of the above mapping directions use Jackson's `ObjectMapper#convertValue()` to perform the
 * mapping. See the Jackson documentation for information on the impact of field and constructor visibility, caveats
 * on serialization and deserialization across object references, etc.
 *
 * However, in the simplest case, both `mapFrom` and `mapTo` should succeed if all fields of the Java class are
 * public (or have public getters/setters), and if there is a public default constructor (or no defined constructors).
 *
 * Referenced objects will be transitively serialized/deserialized to/from nested JSON objects as
 * long as the object graph is acyclic.
 *
 * ==== Encoding a JSON object to a String
 *
 * You use {@link io.vertx.core.json.JsonObject#encode} to encode the object to a String form.
 *
 * === JSON arrays
 *
 * The {@link io.vertx.core.json.JsonArray} class represents JSON arrays.
 *
 * A JSON array is a sequence of values (string, number, boolean).
 *
 * JSON arrays can also contain null values.
 *
 * ==== Creating JSON arrays
 *
 * Empty JSON arrays can be created with the default constructor.
 *
 * You can create a JSON array from a string JSON representation as follows:
 *
 * [source,java]
 * ----
 * {@link docoverride.json.Examples#example0_2}
 * ----
 *
 * ==== Adding entries into a JSON array
 *
 * You add entries to a JSON array using the {@link io.vertx.core.json.JsonArray#add} methods.
 *
 * [source,java]
 * ----
 * {@link docoverride.json.Examples#example3}
 * ----
 *
 * ==== Getting values from a JSON array
 *
 * You get values from a JSON array using the {@code getXXX} methods, for example:
 *
 * [source,java]
 * ----
 * {@link docoverride.json.Examples#example4}
 * ----
 *
 * ==== Encoding a JSON array to a String
 *
 * You use {@link io.vertx.core.json.JsonArray#encode} to encode the array to a String form.
 *
 *
 */
@Document(fileName = "override/json.adoc")
package docoverride.json;

import io.vertx.docgen.Document;

