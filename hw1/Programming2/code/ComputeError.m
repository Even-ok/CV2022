function error = ComputeError(Affine, p1, p2, match)
% Compute the error using transformation matrix Affine to 
% transform the point in p1 to its matching point in p2.
%
% INPUT:
%   Affine:  3 x 3 affain transformation matrix
%   p1: N1 * 2 point matrix
%   p2: N2 * 2 point matrix
%   match:  M x 2 matrix, each row represents a match
% OUTPUT:
%   error: M * 1 matrix where error(i) is the error of fitting the i-th 
%   match to the given transformation matrix, with Euclidean distance.
%


    % Affine * x(dim: 3 * N) = b1;    x'(dim: N * 3) * Affine' = b1'
    p1_trans = [p1(match(:,1),:) ones(length(match),1)]*Affine';
    p2_match = p2(match(:,2),:);
    % Euclian distance
    error = sqrt(sum((p1_trans(:,1:2) - p2_match).^2,2));
    
end