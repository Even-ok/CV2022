clc;
clear;
close all;
%%  Input data
dataX = [-2 0   2 3   4   5   6    8    10   12  13  14  16  18 ]; 
dataY = [0  0.9 2 6.5 2.9 8.8 3.95 5.03 5.97 7.1 1.2 8.2 8.5 10.1]; 

%% Least squares analysis

% number of point
n=size(dataX,2);
line_vector = LeastSquare(dataX,dataY);

%% Draw the image of LS
figure
plot(dataX,dataY,'o','MarkerFaceColor','r');
hold on

py=line_vector(1,1)*dataX+line_vector(1,2);
plot(dataX,py,'m')
hold on

%% Start ransac

iterations = 100;
k=0;
best_setx = [];
best_sety = [];
best_model = [];
innerPointSum = 0;      
pointThres = round(0.8 * n);
distThres = 0.5;
iniPointNum = 2; % The data points for instantiating the model
while  k<iterations
    [init_inliersx,init_inliersy,init_outliersx,init_outliersy] = ramdomSelect(dataX,dataY,iniPointNum,n);
    s1 = LeastSquare(init_inliersx,init_inliersy); 
    
    % Calculate the consensus sets
    Consensus_Setx = init_inliersx;
    Consensus_Sety = init_inliersy;
    [m,n] = size(init_outliersx);
    for i=1:m
        dist = abs(line_vector(1,1)*init_outliersx(i,1)+line_vector(1,2)-init_outliersy(i,1))/sqrt(line_vector(1,1)^2+1);%Judge the distance from the point to the line  
        if (dist < distThres)
            Consensus_Setx = [Consensus_Setx;init_outliersx(i,1)];  % If the distance is less than threshold, add him to the consensus set
            Consensus_Sety = [Consensus_Sety;init_outliersy(i,1)];
        end
        
        [pointSize,~] = size(Consensus_Setx);
        if pointSize >= pointThres
            break;
        end
    end
    
    % Get best inliers
    [m1,n1] = size(Consensus_Setx);
    if (m1 > innerPointSum)
        innerPointSum = m1;
        best_consensus_setx = Consensus_Setx;
        best_consensus_sety = Consensus_Sety;
    end
    if m1 >= pointThres
        break;
    end
    k=k+1;
end

%% Draw the image of RANSAC 
line_vector=LeastSquare(best_consensus_setx,best_consensus_sety);
py=line_vector(1,1)*dataX+line_vector(1,2);
plot(dataX,py,'b')
hold on


