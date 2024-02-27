/*
 * Testerra
 *
 * (C) 2023, Selina Natschke, Deutsche Telekom MMS GmbH, Deutsche Telekom AG
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import {autoinject} from 'aurelia-framework';
import {StatisticsGenerator} from "../../services/statistics-generator";
import {data} from "../../services/report-model";
import ISessionContext = data.SessionContext;
import "./sessions.scss"

@autoinject()
export class Video {
    private _sessionContexts:ISessionContext[];

    constructor(
        private _statistics: StatisticsGenerator,
    ) {
    }

    activate(
        params: any,
    ) {
        this._statistics.getMethodDetails(params.methodId).then(methodDetails => {
            this._sessionContexts = methodDetails.sessionContexts;
        });
    }
}
