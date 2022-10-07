function H = getAffineMatrix(P1, P2)
% getAffineMatrix: Computes the transformation matrix between Point 1 and
% Point 2
%Input:
%   P1: N * 2 matrix, each row is a point in image 1 
%   P2: N * 2 matrix, each row is the point in image 2 that 
%       matches the same point in image 1
%Output:
%   H: 3 * 3 affine transformation matrix, 



    N = size(P1,1);
    if size(P1, 1) ~= size(P2, 1),
        error('The dimensions of two matrix is unmatched.');
    elseif N<3   % the DOF of the affine transformation is 6, thus 3 points
        error('At least 3 points are required.');
    end
    
    % Convert the input points to homogeneous coordintes.
    P_1 = [P1';ones(1,N)];
    P_2 = [P2';ones(1,N)];
    
    % H * P1 = P2; H = P2 * (P1)^(-1) = P2 / P1; 
    % If P_1 is square and the solution exists, the return satisfies H *
    % P_1 = P_2, else the return is the least squares solution.
    H = P_2 / P_1; 
    H(3,:) = [0 0 1];
end
    
