/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

mica.config
  .factory('MicaConfigResource', ['$resource',
    function ($resource) {
      return $resource('ws/config', {}, {
        // override $resource.save method because it uses POST by default
        'save': {method: 'PUT'},
        'get': {method: 'GET'}
      });
    }])
  .factory('MicaConfigOpalProjectsResource', ['$resource',
    function ($resource) {
      return $resource('ws/config/opal-projects', {}, {
        'get': {method: 'GET', isArray: true}
      });
    }])
  .factory('PublicMicaConfigResource', ['$resource',
    function ($resource) {
      return $resource('ws/config/_public', {}, {
        'get': {method: 'GET'}
      });
    }])
  .factory('KeyStoreResource', ['$resource',
    function ($resource) {
      return $resource('ws/config/keystore/system/https', {}, {
        'save': {method: 'PUT'}
      });
    }])
  .factory('OpalCredentialsResource', ['$resource',
    function ($resource) {
      return $resource('ws/config/opal-credentials', {}, {});
    }])
  .factory('OpalCredentialResource', ['$resource',
    function ($resource) {
      return $resource('ws/config/opal-credential/:id', {id: '@id'}, {});
    }])
  .factory('TranslationsResource', ['$resource',
    function ($resource) {
      return $resource('ws/config/i18n/:id.json');
    }])
  .factory('CustomTranslationsResource', ['$resource',
    function ($resource) {
      return $resource('ws/config/i18n/custom/:locale.json', {locale: '@locale'}, {
        'save': {method: 'PUT', params: {merge: false}},
        'import': {method: 'PUT', url: 'ws/config/i18n/custom/import', params: {merge: false}},
        'export': {method: 'GET', url: 'ws/config/i18n/custom/export'}
      }, {});
    }])
  .factory('StyleEditorService', [
    function () {
      return {
        /**
         * HACK until angular-ui-ce can config path settings
         */
        configureAcePaths: function () {
          var defaultPath = ace.config.get('basePath');

          if (defaultPath.indexOf('bower_components') === -1) {
            // production path must be changed
            ace.config.set('basePath', '/scripts');
            ace.config.set('modePath', '/scripts');
            ace.config.set('themePath', '/scripts');
            ace.config.set('workerPath', '/scripts');
          }
        },

        getEditorOptions: function () {
          return {
            options: {
              theme: 'monokai',
              mode: 'css',
              displayIndentGuides: true,
              useElasticTabstops: true
            }
          };
        }
      };
    }])

  .provider('MicaConfigProxy',
    function () {
      function Proxy(MicaConfigResource, $filter, $q) {
        this.languages = function() {
          var deferred = $q.defer();
          MicaConfigResource.get(function (micaConfig) {
            var formLanguages = {};
            micaConfig.languages.forEach(function (loc) {
              formLanguages[loc] = $filter('translate')('language.' + loc);
            });
            deferred.resolve(formLanguages);
          });

          return deferred.promise;
        };
      }

      this.$get = ['MicaConfigResource', '$filter', '$q', function(MicaConfigResource, $filter, $q) {
        return new Proxy(MicaConfigResource, $filter, $q);
      }];
    });
