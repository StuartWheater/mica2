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

mica.dataset = angular.module('mica.dataset', [
  'mica.config',
  'obiba.form',
  'obiba.mica.localized',
  'mica.publish',
  'mica.attributes',
  'mica.commons',
  'obiba.notification',
  'ngResource',
  'ngRoute',
  'ui.bootstrap',
  'ui',
  'pascalprecht.translate',
  'mica.status',
  'mica.revisions',
  'mica.network'
]);
