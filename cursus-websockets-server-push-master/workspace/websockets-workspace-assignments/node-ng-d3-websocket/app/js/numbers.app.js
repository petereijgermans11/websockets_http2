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
	// TODO create a listener for a socket open event
	// and make it:
	// - send a datagram with contents: $scope.expression
     // - execute $scope.running = true;
      }

	// TODO create listener for socket close and error events
	// and make them log the event to the console

	// TODO create a listener for a socket message event
	// and make it execute:
        $scope.$apply(function() {
          $scope.numbers = e.data;
        });
      
    };

  }]);
