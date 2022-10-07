function H = RANSAC(p1, p2, match, maxIter, chooseNum, innerThreshold, setSizeThreshold)
% RANSAC: Robust fit a model to a data set S which contains outliers
%
% INPUT: 
%  p1 - point matrix of size N1 * 2
%  p2 - point matrix of size N2 * 2
%  match - the match matrix of size M * 2, whose row represents a match between the descriptors [p1_i,p2_j]
%  maxIter - the number of iterations RANSAC will run
%  chooseNum - the number of randomly-chosen in each data set
%  innerThreshold - a threshold of the distance from the point to the model
%  setSizeThreshold - a size threshold of a data set
%
% OUTPUT: 
%   H - a robust estimation of affine transformation from p1 to p2

    % the number of matched descriptors
    N = size(match, 1);
    if N<4
        errorValue('Too few matches to generate a transformation matrix!')
    end
    if ~exist('maxIter', 'var'),
        maxIter = 200;
    end
    if ~exist('chooseNum', 'var'),
        chooseNum = ceil(0.2 * N);
    end
    chooseNum = max(chooseNum,3);
    if ~exist('innerThreshold', 'var'),
        innerThreshold = 30;
    end
    if ~exist('setSizeThreshold', 'var'),
        setSizeThreshold = floor(0.7 * N);
    end
    
    maxError = Inf;
    for i = 1 : maxIter
        % divide match matrix into 2 parts randomly
        idx = randperm(size(match, 1));
        part1 = match(idx(1:chooseNum), :);
        part2 = match(idx(chooseNum+1:end), :);
        affine = getAffineMatrix(p1(part1(:, 1), :), p2(part1(:, 2), :));
        errorValue = ComputeError(affine,p1,p2,part2);
        rightPoint = errorValue<=innerThreshold;
         if sum(rightPoint(:)) + part1>= setSizeThreshold
             pointSum = [part1;part2(rightPoint,:)];
             % re-estimate the model using all points in the consensus set
             newAffain = getAffineMatrix(p1(pointSum(:, 1), :), p2(pointSum(:, 2), :));
             % re-calculate the error of new model
             newError = sum(ComputeError(newAffain, p1, p2, pointSum));
             % select the optimal consistent set
            if newError < maxError
                H = newAffain;
                maxError = newError;
            end
         end
    end
         
    if sum(sum((H - eye(3)).^2)) == 0,
        disp('No RANSAC fit was found.')
    end
end
        
