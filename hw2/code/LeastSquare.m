function  line_vector=LeastSquare(dataX,dataY)
% The least square formula and derivation are written in readme
% Input:
%     dataX - abscissa
%     dataY - ordinate
%
% Output:
%     line_vector - [a,b] represent the slope and intercept of the line, respectively


dataX=dataX';
dataY=dataY';
n=size(dataX,2);

% a*¦²xi^2 + b*¦²xi = ¦²(xi*yi)
% a*¦²xi + b*N = ¦²yi
x2=sum(dataX.^2); % ¦²(xi^2)
x1=sum(dataX); % ¦²(xi)
x1y1=sum(dataX.*dataY); % ¦²(xi*yi)
y1=sum(dataY); % ¦²(yi)
a=(n*x1y1-x1*y1)/(n*x2-x1*x1); % The slope
b=(y1-a*x1)/n; % intercept 
line_vector=[a,b];

end