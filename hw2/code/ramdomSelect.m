function [init_inliersx,init_inliersy,init_outliersx,init_outliersy] = ramdomSelect(dataX,dataY,Num,Numtotal)

% This function selects num points to initialize the model and divide the initial Inliers and outliners
% 
% Input:
%     dataX - abscissa
%     dataY - ordinate
%     Num - the number of initialization points
%     Numtotal - the number of total points
%     
% Output:
%     init_inliersx - The x-coordinate of the set of points used to initialize the model
%     init_inliersy - The y-coordinate of the set of points used to initialize the model
%     init_outliersx - The x-coordinates of the remaining points in the set
%     init_outliersy - The y-coordinates of the remaining points in the set

[m,n] = size(dataX);
RandomNumLen = 100;                                        %The length of the generated random number
RandomMat = 1+(Numtotal-1)*rand(1,RandomNumLen);           %Generate 100 random numbers 
RandomMat = round(RandomMat);
RandomMat_Num = zeros(1,Num);                              %Pick Num non-repeating random numbers from it 
k=1;
for i=1:RandomNumLen                                       %The current number, which does not appear in Num random numbers, is added
   if((isInVec(RandomMat(1,i),RandomMat_Num) == 0)&& (k<=Num) ) 
       RandomMat_Num(1,k) = RandomMat(1,i);
       k=k+1;
   end
end
tem_inx =[];
tem_iny =[];
tem_outx =[];
tem_outy =[];    
for i=1:n                                                 
    if(isInVec(i,RandomMat_Num) > 0)
        tem_inx = [tem_inx;dataX(1,i)];
        tem_iny = [tem_iny;dataY(1,i)];
    else
        tem_outx = [tem_outx;dataX(1,i)];
        tem_outy = [tem_outy;dataY(1,i)];
    end
end
init_inliersx = tem_inx;
init_inliersy = tem_iny;
init_outliersx = tem_outx;
init_outliersy = tem_outy;