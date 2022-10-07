function blobSet = detect_point_scale(im, scaleLevel, threshold)

% This function implements LoG to find blobs across scale space, by Scaling 
% the filters.
% 
% Input:
%     im - Input image
%     
% Output:
%     blobSet - an array of size [n,4] representing (x, y, radius, point_value)
    
%% Generate Gaussian filter
% gray scale
if size(im,3) > 1
    im = rgb2gray(im);
    im = im2double(im);
end
minSigma = 2;% The minimum sigma
c = 1.2; %constant
n = scaleLevel;

% sigma matrix
sigma = minSigma * c .^(0:n-1);
[h,w] = size(im);

% Create scale space
scaleSpace = zeros(h,w,n);
for i = 1:n
    % A rotationally symmetric Laplacian of Gaussian filter of size 
    % hsize with standard deviation sigma. We normalized it.
    hsize = 2*ceil(3*sigma(i)) + 1;  % Gaussian window size
    Laplacian = sigma(i)^2*fspecial('log',hsize,sigma(i));
    
    % Get positive LoG responses
    scaleSpace(:,:,i) = imfilter(im,Laplacian,'same','replicate').^2;
end

%% Non-maximum suppression
% find the largest LoG response in a specific fieid.
maxPoints = zeros(h,w,n);

% Actually, we can change the order of next two cycles. One for space, one
% for scale.

% Find the maximum value in a 3 ¡Á 3 sliding window 
% as a uniform value for each window
for i = 1:n
    maxPoints(:,:,i) = ordfilt2(scaleSpace(:,:,i),9,ones(3));
end
% Find the maximum value in the scale space (3 level)
for i = 1:n
    maxPoints(:,:,i) = max(maxPoints(:,:,max(1,i-1):min(n,i+1)),[],3);
end
maxPoints = maxPoints .* (maxPoints == scaleSpace);

%% Generate interest point

blobSet = [];
for i=1:n
    scale_img = maxPoints(:,:,i);
    % Find the row and column of the non-zero element of the matrix
    [row,col] = find(scale_img >= threshold);
    % Find the idx of interest point in a specific scale
    idx = sub2ind(size(scale_img),row,col);
    % Add every point iteratively
    blobSet = [blobSet; col,row,ones(length(col),1)*sigma(i)*sqrt(2),scale_img(idx)];
end

[numblobs,~] = size(blobSet);
if numblobs < 1000
    fprintf('number of blobs: %d (< 1000),please retry a smaller threshold :p\n',numblobs);
end
end


