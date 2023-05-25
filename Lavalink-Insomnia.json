{
    "_type": "export",
    "__export_format": 4,
    "__export_date": "2023-05-25T08:32:55.525Z",
    "__export_source": "insomnia.desktop.app:v2023.2.2",
    "resources": [
        {
            "_id": "ws-req_e2443b156d17408186224677aa3c9752",
            "parentId": "fld_670ce7a796714ccea2b1b2aa590fb79a",
            "modified": 1685003545245,
            "created": 1667222420082,
            "name": "Websocket",
            "url": "{{WS}}{{VERSION}}/websocket",
            "metaSortKey": -1667222420082,
            "headers": [
                {
                    "id": "pair_a949d4e1089147a49d9de1a4684046b0",
                    "name": "User-Id",
                    "value": "{{USER_ID}}",
                    "description": ""
                }
            ],
            "authentication": {
                "type": "apikey",
                "disabled": false,
                "key": "Authorization",
                "value": "{{AUTHORIZATION}}",
                "addTo": "header"
            },
            "parameters": [],
            "settingEncodeUrl": true,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingFollowRedirects": "global",
            "description": "",
            "_type": "websocket_request"
        },
        {
            "_id": "fld_670ce7a796714ccea2b1b2aa590fb79a",
            "parentId": "wrk_bc1c8324b157461ab6b17e9a6a43799b",
            "modified": 1685002578577,
            "created": 1667221682436,
            "name": "Lavalink",
            "description": "",
            "environment": {
                "URL": "http://localhost:2333",
                "WS": "ws://localhost:2333",
                "VERSION": "/v4",
                "SESSION_ID": "x8iy1ifwdiez4ats",
                "USER_ID": "817403182526365706",
                "GUILD_ID": "817327181659111454",
                "AUTHORIZATION": "youshallnotpass"
            },
            "environmentPropertyOrder": {
                "&": [
                    "URL",
                    "WS",
                    "VERSION",
                    "SESSION_ID",
                    "USER_ID",
                    "GUILD_ID",
                    "AUTHORIZATION"
                ]
            },
            "metaSortKey": -1667221682436,
            "_type": "request_group"
        },
        {
            "_id": "wrk_bc1c8324b157461ab6b17e9a6a43799b",
            "parentId": null,
            "modified": 1667221722963,
            "created": 1667221685565,
            "name": "Lavalink",
            "description": "",
            "scope": "collection",
            "_type": "workspace"
        },
        {
            "_id": "req_3592759af628411c912660efe9bc16db",
            "parentId": "fld_da8d788c6bff49c191561e15b6c85024",
            "modified": 1667222898212,
            "created": 1667221682433,
            "url": "{{URL}}{{VERSION}}/routeplanner/status",
            "name": "/routeplanner/status",
            "description": "",
            "method": "GET",
            "body": {},
            "parameters": [],
            "headers": [],
            "authentication": {
                "type": "apikey",
                "disabled": false,
                "key": "Authorization",
                "value": "{{AUTHORIZATION}}",
                "addTo": "header"
            },
            "metaSortKey": -1667221682433,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "fld_da8d788c6bff49c191561e15b6c85024",
            "parentId": "fld_670ce7a796714ccea2b1b2aa590fb79a",
            "modified": 1667221682434,
            "created": 1667221682434,
            "name": "Route Planner",
            "description": "",
            "environment": {},
            "environmentPropertyOrder": null,
            "metaSortKey": -1667221682434,
            "_type": "request_group"
        },
        {
            "_id": "req_2682c1c7fe3c46f3a79b8de2e4817b40",
            "parentId": "fld_da8d788c6bff49c191561e15b6c85024",
            "modified": 1667222900520,
            "created": 1667221682432,
            "url": "{{URL}}{{VERSION}}/routeplanner/free/address",
            "name": "/routeplanner/free/address",
            "description": "",
            "method": "POST",
            "body": {},
            "parameters": [],
            "headers": [],
            "authentication": {
                "type": "apikey",
                "disabled": false,
                "key": "Authorization",
                "value": "{{AUTHORIZATION}}",
                "addTo": "header"
            },
            "metaSortKey": -1667221682432,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "req_77f536212f224826ad7e6fc0645ee916",
            "parentId": "fld_da8d788c6bff49c191561e15b6c85024",
            "modified": 1667222902587,
            "created": 1667221682431,
            "url": "{{URL}}{{VERSION}}/routeplanner/free/all",
            "name": "/routeplanner/free/all",
            "description": "",
            "method": "POST",
            "body": {},
            "parameters": [],
            "headers": [],
            "authentication": {
                "type": "apikey",
                "disabled": false,
                "key": "Authorization",
                "value": "{{AUTHORIZATION}}",
                "addTo": "header"
            },
            "metaSortKey": -1667221682431,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "req_c32c25aaffb74d2185cc5a9c92d872c3",
            "parentId": "fld_5ddb4a0525c74c9296e6a079539b7f23",
            "modified": 1685001590944,
            "created": 1667221682429,
            "url": "{{URL}}{{VERSION}}/sessions/{{SESSION_ID}}/players",
            "name": "/sessions/{{SESSION_ID}}/players",
            "description": "",
            "method": "GET",
            "body": {},
            "parameters": [],
            "headers": [],
            "authentication": {
                "type": "apikey",
                "disabled": false,
                "key": "Authorization",
                "value": "{{AUTHORIZATION}}",
                "addTo": "header"
            },
            "metaSortKey": -1676111589495,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "fld_5ddb4a0525c74c9296e6a079539b7f23",
            "parentId": "fld_57411f1051aa4153a7da1d430e0dd8b1",
            "modified": 1685001496561,
            "created": 1685001496561,
            "name": "Players",
            "description": "",
            "environment": {},
            "environmentPropertyOrder": null,
            "metaSortKey": -1685001496561,
            "_type": "request_group"
        },
        {
            "_id": "fld_57411f1051aa4153a7da1d430e0dd8b1",
            "parentId": "fld_670ce7a796714ccea2b1b2aa590fb79a",
            "modified": 1667221682430,
            "created": 1667221682430,
            "name": "Sessions",
            "description": "",
            "environment": {},
            "environmentPropertyOrder": null,
            "metaSortKey": -1667221682430,
            "_type": "request_group"
        },
        {
            "_id": "req_ebf4817f8e05430291d9c3665f3aed06",
            "parentId": "fld_5ddb4a0525c74c9296e6a079539b7f23",
            "modified": 1685001731181,
            "created": 1667221682428,
            "url": "{{URL}}{{VERSION}}/sessions/{{SESSION_ID}}/players/{{GUILD_ID}}",
            "name": "/sessions/{{SESSION_ID}}/players/{{GUILD_ID}}",
            "description": "",
            "method": "PATCH",
            "body": {
                "mimeType": "application/json",
                "text": "{\n  \"encodedTrack\": \"...\",\n  \"identifier\": \"...\",\n  \"startTime\": 0,\n  \"endTime\": 0,\n  \"volume\": 100,\n  \"position\": 0,\n  \"paused\": false,\n  \"filters\": {\n\t\t\"volume\": 1.0,\n\t\t\"equalizer\": [\n\t\t\t{\n\t\t\t\t\"band\": 0,\n\t\t\t\t\"gain\": 0.2\n\t\t\t}\n\t\t],\n\t\t\"karaoke\": {\n\t\t\t\"level\": 1.0,\n\t\t\t\"monoLevel\": 1.0,\n\t\t\t\"filterBand\": 220.0,\n\t\t\t\"filterWidth\": 100.0\n\t\t},\n\t\t\"timescale\": {\n\t\t\t\"speed\": 1.0,\n\t\t\t\"pitch\": 1.0,\n\t\t\t\"rate\": 1.0\n\t\t},\n\t\t\"tremolo\": {\n\t\t\t\"frequency\": 2.0,\n\t\t\t\"depth\": 0.5\n\t\t},\n\t\t\"vibrato\": {\n\t\t\t\"frequency\": 2.0,\n\t\t\t\"depth\": 0.5\n\t\t},\n\t\t\"rotation\": {\n\t\t\t\"rotationHz\": 0\n\t\t},\n\t\t\"distortion\": {\n\t\t\t\"sinOffset\": 0.0,\n\t\t\t\"sinScale\": 1.0,\n\t\t\t\"cosOffset\": 0.0,\n\t\t\t\"cosScale\": 1.0,\n\t\t\t\"tanOffset\": 0.0,\n\t\t\t\"tanScale\": 1.0,\n\t\t\t\"offset\": 0.0,\n\t\t\t\"scale\": 1.0\n\t\t},\n\t\t\"channelMix\": {\n\t\t\t\"leftToLeft\": 1.0,\n\t\t\t\"leftToRight\": 0.0,\n\t\t\t\"rightToLeft\": 0.0,\n\t\t\t\"rightToRight\": 1.0\n\t\t},\n\t\t\"lowPass\": {\n\t\t\t\"smoothing\": 20.0\n\t\t}\n\t},\n  \"voice\": {\n    \"token\": \"...\",\n    \"endpoint\": \"...\",\n    \"sessionId\": \"...\"\n  }\n}"
            },
            "parameters": [
                {
                    "id": "pair_d4fdc3f7c15c415683a327150c419fe1",
                    "name": "noReplace",
                    "value": "false",
                    "description": ""
                }
            ],
            "headers": [
                {
                    "name": "Content-Type",
                    "value": "application/json",
                    "id": "pair_f7483c3bd7a04440b81ef19799a4514b"
                }
            ],
            "authentication": {
                "type": "apikey",
                "disabled": false,
                "key": "Authorization",
                "value": "{{AUTHORIZATION}}",
                "addTo": "header"
            },
            "metaSortKey": -1676111589445,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "req_a685f7bb2e6a4b02a3a2980442a5f050",
            "parentId": "fld_5ddb4a0525c74c9296e6a079539b7f23",
            "modified": 1685001604389,
            "created": 1667221682426,
            "url": "{{URL}}{{VERSION}}/sessions/{{SESSION_ID}}/players/{{GUILD_ID}}",
            "name": "/sessions/{{SESSION_ID}}/players/{{GUILD_ID}}",
            "description": "",
            "method": "DELETE",
            "body": {},
            "parameters": [],
            "headers": [],
            "authentication": {
                "type": "apikey",
                "disabled": false,
                "key": "Authorization",
                "value": "{{AUTHORIZATION}}",
                "addTo": "header"
            },
            "metaSortKey": -1676111589395,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "req_57a3dc3a2f24431f941c21fe1bb61d91",
            "parentId": "fld_5ddb4a0525c74c9296e6a079539b7f23",
            "modified": 1685001606090,
            "created": 1667221682425,
            "url": "{{URL}}{{VERSION}}/sessions/{{SESSION_ID}}/players/{{GUILD_ID}}",
            "name": "/sessions/{{SESSION_ID}}/players/{{GUILD_ID}}",
            "description": "",
            "method": "GET",
            "body": {},
            "parameters": [],
            "headers": [
                {
                    "id": "pair_42793016a1914fb0aa9398fac88bdc73",
                    "name": "Authorization",
                    "value": "{{ AUTHORIZATION }}",
                    "description": ""
                }
            ],
            "authentication": {},
            "metaSortKey": -1676111589345,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "req_74b3af1fbf1748faa22d4b77e1ab8d76",
            "parentId": "fld_57411f1051aa4153a7da1d430e0dd8b1",
            "modified": 1685001777656,
            "created": 1667221682424,
            "url": "{{URL}}{{VERSION}}/sessions/{{SESSION_ID}}",
            "name": "/sessions/{{SESSION_ID}}",
            "description": "",
            "method": "PATCH",
            "body": {
                "mimeType": "application/json",
                "text": "{\n\t\"resuming\": true,\n\t\"timeout\": 60\n}"
            },
            "parameters": [],
            "headers": [
                {
                    "id": "pair_969f09815627417eafbf7980f4a56d2c",
                    "name": "Authorization",
                    "value": "{{ AUTHORIZATION }}",
                    "description": ""
                },
                {
                    "name": "Content-Type",
                    "value": "application/json"
                }
            ],
            "authentication": {},
            "metaSortKey": -1667221682424,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "req_06f7a987c18a488bbf9e428c5e64c8a0",
            "parentId": "fld_670ce7a796714ccea2b1b2aa590fb79a",
            "modified": 1685001320787,
            "created": 1667221682423,
            "url": "{{URL}}/version",
            "name": "/version",
            "description": "",
            "method": "GET",
            "body": {},
            "parameters": [],
            "headers": [
                {
                    "id": "pair_1bc1d4df671045a5b7e45b0314b20f7c",
                    "name": "Authorization",
                    "value": "{{AUTHORIZATION}}",
                    "description": ""
                }
            ],
            "authentication": {},
            "metaSortKey": -1667221682423,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "req_971b34b546634c53ad355d416eadc0b8",
            "parentId": "fld_670ce7a796714ccea2b1b2aa590fb79a",
            "modified": 1685003506452,
            "created": 1667221682422,
            "url": "{{ URL }}{{VERSION}}/info",
            "name": "/info",
            "description": "",
            "method": "GET",
            "body": {},
            "parameters": [],
            "headers": [],
            "authentication": {
                "type": "apikey",
                "disabled": false,
                "key": "Authorization",
                "value": "{{AUTHORIZATION}}",
                "addTo": "header"
            },
            "metaSortKey": -1667221682422,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "req_58375f725da547c5a39671bcabe773eb",
            "parentId": "fld_670ce7a796714ccea2b1b2aa590fb79a",
            "modified": 1684840131372,
            "created": 1667221682421,
            "url": "{{URL}}{{VERSION}}/loadtracks",
            "name": "/loadtracks",
            "description": "",
            "method": "GET",
            "body": {},
            "parameters": [
                {
                    "id": "pair_2e73443a77ed42518b4ff5ce573b5731",
                    "name": "identifier",
                    "value": "ytmsearch:\"DED831500883\"",
                    "description": ""
                }
            ],
            "headers": [],
            "authentication": {
                "type": "apikey",
                "disabled": false,
                "key": "Authorization",
                "value": "{{AUTHORIZATION}}",
                "addTo": "header"
            },
            "metaSortKey": -1667221682421,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "req_20a98fd2674c4e5782779c8b7ab7d0fb",
            "parentId": "fld_670ce7a796714ccea2b1b2aa590fb79a",
            "modified": 1674472021244,
            "created": 1667221682420,
            "url": "{{URL}}{{VERSION}}/decodetrack",
            "name": "/decodetrack",
            "description": "",
            "method": "GET",
            "body": {},
            "parameters": [
                {
                    "id": "pair_33f3b72bc4ee4ce8b791eb961031c0ee",
                    "name": "track",
                    "value": "QAAAgQIAGEFyY2hpdGVjdHMgLSAiZGVlcCBmYWtlIgAPRXBpdGFwaCBSZWNvcmRzAAAAAAADQ/AAC1RuQ1VKVlBRSDNJAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9VG5DVUpWUFFIM0kAB3lvdXR1YmUAAAAAAAAAAA==",
                    "description": ""
                }
            ],
            "headers": [],
            "authentication": {
                "type": "apikey",
                "disabled": false,
                "key": "Authorization",
                "value": "{{AUTHORIZATION}}",
                "addTo": "header"
            },
            "metaSortKey": -1667221682420,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "req_4382bb60e5a242eeb3ae2ce42c5e624c",
            "parentId": "fld_670ce7a796714ccea2b1b2aa590fb79a",
            "modified": 1685001914907,
            "created": 1667221682418,
            "url": "{{URL}}{{VERSION}}/decodetracks?trace=true",
            "name": "/decodetracks",
            "description": "",
            "method": "POST",
            "body": {
                "mimeType": "application/json",
                "text": "[\n\t\"QAAAgQIAGEFyY2hpdGVjdHMgLSAiZGVlcCBmYWtlIgAPRXBpdGFwaCBSZWNvcmRzAAAAAAADQ/AAC1RuQ1VKVlBRSDNJAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9VG5DVUpWUFFIM0kAB3lvdXR1YmUAAAAAAAAAAA==\"\n]"
            },
            "parameters": [],
            "headers": [
                {
                    "name": "Content-Type",
                    "value": "application/json"
                }
            ],
            "authentication": {
                "type": "apikey",
                "disabled": false,
                "key": "Authorization",
                "value": "{{AUTHORIZATION}}",
                "addTo": "header"
            },
            "metaSortKey": -1667221682418,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "req_f82090b6dd6a4eb98f12ea73568fd206",
            "parentId": "fld_670ce7a796714ccea2b1b2aa590fb79a",
            "modified": 1667223574749,
            "created": 1667221682417,
            "url": "{{URL}}{{VERSION}}/stats",
            "name": "/stats",
            "description": "",
            "method": "GET",
            "body": {},
            "parameters": [],
            "headers": [],
            "authentication": {
                "type": "apikey",
                "disabled": false,
                "key": "Authorization",
                "value": "{{AUTHORIZATION}}",
                "addTo": "header"
            },
            "metaSortKey": -1667221682368,
            "isPrivate": false,
            "settingStoreCookies": true,
            "settingSendCookies": true,
            "settingDisableRenderRequestBody": false,
            "settingEncodeUrl": true,
            "settingRebuildPath": true,
            "settingFollowRedirects": "global",
            "_type": "request"
        },
        {
            "_id": "env_cd9fdf4434dd8b3dacfd432b24ac0b4ed5029973",
            "parentId": "wrk_bc1c8324b157461ab6b17e9a6a43799b",
            "modified": 1685001348743,
            "created": 1667221691915,
            "name": "Base Environment",
            "data": {
                "URL": "http://localhost:2333",
                "WS": "ws://localhost:2333",
                "AUTHORIZATION": "youshallnotpass",
                "VERSION": "/v4",
                "SESSION_ID": "x8iy1ifwdiez4ats",
                "USER_ID": "817403182526365706",
                "GUILD_ID": "817327181659111454"
            },
            "dataPropertyOrder": {
                "&": [
                    "URL",
                    "WS",
                    "AUTHORIZATION",
                    "VERSION",
                    "SESSION_ID",
                    "USER_ID",
                    "GUILD_ID"
                ]
            },
            "color": null,
            "isPrivate": false,
            "metaSortKey": 1667221691915,
            "_type": "environment"
        }
    ]
}