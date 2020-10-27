﻿using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TestViewer.model.jsap
{
    class JsapUpdate : IJsapRequest
    {
        private String sparql;
        public JsapUpdate(JObject obj) {

            sparql = obj.GetValue("sparql").Value<String>();
            //obj.GetValue("forcedBindings");
        }
        public List<JsapBind> getBinds()
        {
            throw new NotImplementedException();
        }

        public string getSparql()
        {
            return sparql;
        }
    }
}
