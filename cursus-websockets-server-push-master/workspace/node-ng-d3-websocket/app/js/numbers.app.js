angular.module('numbers.app', ['d3.directives', 'numbers.directives'])
  .controller('NumbersCtrl', ['$scope', function($scope) {
    function createWebSocket(path) {
      var host = window.location.hostname;
      if (host == '') host = 'localhost';
      var uri = 'ws://' + host + ':9160' + path;

      var Socket = "MozWebSocket" in window ? MozWebSocket : WebSocket;
      return new Socket(uri);
    }

    $scope.numbers = {};
    $scope.expression = '{"repeat":25,"delay":1000,"max":40,"size":20}';
    $scope.running = false;

    var socket = null;
    $scope.stop = function() {
      if (socket != null) socket.close();
      socket = null;
      $scope.running = false;
    };

    $scope.executeExpression = function() {
      if (socket != null) socket.close();
      socket = createWebSocket("/");
      socket.onopen = function(e) { 
        socket.send($scope.expression);
        $scope.running = true;
      }
      socket.onclose = function(e) { console.log(e); }
      socket.onerror = function(e) { console.log(e); }
      socket.onmessage = function(e) {
        $scope.$apply(function() {
          $scope.numbers = e.data;
        });
      };
    };

  }]);
