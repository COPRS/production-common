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

package esa.s1pdgs.cpoc.common;

public class BrowseImage {

	private BrowseImage() {}
	
	public static final String S1_BROWSE_IMAGE_DIRECTORY = "preview";
	public static final String S1_BROWSE_IMAGE_FORMAT = ".png";
	public static final String BROWSE_IMAGE_TAG = "_bwi";
	
	public static String s1BrowseImageName(String productName) {
		return productName + BROWSE_IMAGE_TAG + S1_BROWSE_IMAGE_FORMAT; 
	}
	
	public static String browseImagePrefix(String productName) {
		return productName + BROWSE_IMAGE_TAG;
	}
	
}
