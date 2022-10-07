function inplement_draw(im1, blobSet1, im2, blobSet2, numsCount)
% This function draws the blobs in two images.
% 
% Input:
%     im1 - image1
%     blobSet1 - an array of size [n,4] representing (x, y, radius,
%     point_value) in image1
%     im2 - image2
%     blobSet2 - the same as blobSet1
%     numsCount - the number of blobs you want to draw in each image
%     
% Output:
%     blobSet - an array of size [n,4] representing (x, y, radius, point_value)

if nargin < 5
    numsCount = size(blobSet1,1);
end
numsCount = min(numsCount, size(blobSet1,1));

% Convert the image to gray scale
if size(im1, 3) > 1
    im1 = rgb2gray(im1);
end
if size(im2, 3) > 1
    im2 = rgb2gray(im2);
end

if numsCount < 1
    fprintf('There is no blob!');
    return; 
end

% Sort the values of feature points from smallest to largest
[~,order1] = sort(-blobSet1(:,4));
[~,order2] = sort(-blobSet2(:,4));
theta = linspace(0, 2*pi, 24);

subplot(211)
% figure;
title('Blob detection in two different scale image')
imshow(im1); hold on;
for i = 1:numsCount
    % Get the blob characteristic scale(the rarius)
    r = blobSet1(order1(i),3);
    plot(blobSet1(order1(i),1) + r*cos(theta), blobSet1(order1(i),2) + r*sin(theta), 'r-', 'linewidth',2);
end

subplot(212)
imshow(im2); hold on;
for i = 1:numsCount
    % Get the blob characteristic scale(the rarius)
    r = blobSet2(order2(i),3);
    plot(blobSet2(order2(i),1) + r*cos(theta), blobSet2(order2(i),2) + r*sin(theta), 'r-', 'linewidth',2);
end
hold on;