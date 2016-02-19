/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

/*global NgObibaMicaTemplateUrlFactory */
angular.module('obiba.mica.search', [
    'obiba.alert',
    'ui.bootstrap',
    'pascalprecht.translate',
    'templates-ngObibaMica'
  ])
  .config(['$provide', function ($provide) {
    $provide.provider('ngObibaMicaSearchTemplateUrl', new NgObibaMicaTemplateUrlFactory().create(
      {
        list: {header: null, footer: null},
        view: {header: null, footer: null},
        form: {header: null, footer: null}
      }
    ));
  }])
  .config(['$provide', '$injector', function ($provide) {
    $provide.provider('ngObibaMicaSearch', function () {
      var localeResolver = ['LocalizedValues', function(LocalizedValues) {
        return LocalizedValues.getLocal();
      }];

      this.setLocaleResolver = function(resolver) {
        localeResolver = resolver;
      };

      this.$get = ['$q', '$injector', function ngObibaMicaSearchFactory($q, $injector) {
        return {
          getLocale: function(success, error) {
            return $q.when($injector.invoke(localeResolver), success, error);
          }
        };
      }];
    });
  }])
  .run(['GraphicChartsConfigurations',
  function (GraphicChartsConfigurations) {
    GraphicChartsConfigurations.setClientConfig();
  }]);
