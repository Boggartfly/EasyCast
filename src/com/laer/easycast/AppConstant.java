/*
 * Copyright 2014 Parth Sane
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.laer.easycast;
import java.util.Arrays;
import java.util.List;
public class AppConstant {
	 // Number of columns of Grid View
    public static final int NUM_OF_COLUMNS = 3;
 
    // Gridview image padding
    public static final int GRID_PADDING = 8; // in dp
 
    // SD card image directory
    public static final String PHOTO_ALBUM = "sdcard";
 
    // supported file formats
    public static final List<String> FILE_EXTN = Arrays.asList("jpg", "jpeg",
            "png");
}
