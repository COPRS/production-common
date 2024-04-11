/*
 * Copyright 2023 Airbus
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

package esa.s1pdgs.cpoc.preparation.worker.model.pdu;

/**
 * Type of PDU Generation
 * 
 * Frames are numbered, and orbit based. The combination of relativeOrbitNumber
 * and FrameNumber should therefore always result in the same region.
 * 
 * Stripes can either be dump or orbit based and aren't numbered. Most of the
 * time they are used to combine multiple products into one bigger product.
 * 
 * Tiles are defined regions on the planet that have an unique identifier (ex.
 * EUROPE)
 * 
 * @author Julian Kaping
 *
 */
public enum PDUType {
	FRAME, STRIPE, TILE;
}
