
%%  Read picture
%  this is the main project

img1Name = '../picture/butterfly.jpg';
img2Name = '../picture/butterfly_big.png';
im1 = imread(img1Name);
im2 = imread(img2Name);
%imshow(im);

%% Detect interest point and characteristic scale

% Generate Gaussian scale space, where contains a range of filter scales.
% We can calculate the runtime.

scaleLevel = 15;%number of levels
threshold = 0.005;

tic
blobSet1 = detect_point_scale(im1,scaleLevel, threshold);
toc;

tic
blobSet2 = detect_point_scale(im2,scaleLevel, threshold);
toc;

%% Draw interest points and its characteristic scale in the image
% We can change the number of points we want here.
numsCount = 800;

implement_draw(im1, blobSet1, im2, blobSet2, numsCount);



