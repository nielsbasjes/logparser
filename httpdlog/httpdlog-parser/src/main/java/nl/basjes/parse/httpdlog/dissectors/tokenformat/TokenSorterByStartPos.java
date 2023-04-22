/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2023 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.basjes.parse.httpdlog.dissectors.tokenformat;

import java.io.Serializable;
import java.util.Comparator;

public class TokenSorterByStartPos implements Comparator<Token>, Serializable {
    @Override
    public int compare(final Token t1, final Token t2) {

        int startPosDiff = Integer.compare(t1.getStartPos(), t2.getStartPos());
        if (startPosDiff != 0) {
            return startPosDiff;
        }
        int lengthDiff = Integer.compare(t1.getLength(), t2.getLength());
        if (lengthDiff != 0) {
            return lengthDiff;
        }

        return -1 * Integer.compare(t1.getPrio(), t2.getPrio());

    }
}
