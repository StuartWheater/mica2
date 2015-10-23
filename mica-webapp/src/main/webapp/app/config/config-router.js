'use strict';

mica.config
  .config(['$routeProvider',
    function ($routeProvider) {
      $routeProvider
        .when('/admin/general', {
          templateUrl: 'app/config/views/config-view.html',
          controller: 'MicaConfigController'
        })
        .when('/admin/general/edit', {
          templateUrl: 'app/config/views/config-form.html',
          controller: 'MicaConfigEditController'
        })
        .when('/admin/security', {
          templateUrl: 'app/config/views/config-security-view.html',
          controller: 'MicaConfigController'
        });
    }]);
